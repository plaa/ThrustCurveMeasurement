package tcm.arduinoad;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * On startup is written:
 * 
 * Byte 1:
 *   bit 7:     Whether to use external reference (high) or default (low)
 *   bit 6:     Whether to use super-fast 8-bit mode (high) or normal multiplexed (low)
 *   bits 0-5:  Bit mask of inputs to use
 * Byte 2-4: Number of microseconds to delay between reads (MSB first)
 * Byte 5-6: Baud rate to use / 100 (MSB first)
 * 
 * 
 * Read data format:
 * 
 * First byte:
 * 7 - high bit (1)
 * 6 - missed timing bit (1 for fault)
 * 3-5 - input number 0-5
 * 0-2 - most significant three data bits
 * 
 * Second byte:
 * 7 - low bit (0)
 * 0-6 - least significant seven data bits
 */

/**
 * A class that reads data from the serial port and sends it to listeners.
 * <p>
 * Usage:
 *   1. Instantiate
 *   2. Add listeners
 *   3. Call startListener(port, delay)
 *   -- listeners are called in separate thread --
 *   4. Call 
 */
public class SerialDataCommunicator {
	
	// Output debug on port operations
	private static final boolean DEBUG = true;
	
	// Output debug when receiving data
	private static final boolean DEBUG_DATA = false;
	
	// Output debug when byte misses are encountered
	private static final boolean DEBUG_MISSES = false;
	
	// If non-null, data will be written to this file
	private static final String DEBUG_DUMP_FILE = "datadump";
	
	
	public static final int DEFAULT_BUFFER_TIME = 200;
	
	public static final int SERIAL_READ_BUFFER = 1024;
	
	
	/**
	 * The number of inputs available.
	 */
	public static final int INPUTS = 6;
	
	public static final int EXTERNAL_REFERENCE_BIT = 128;
	
	public static final int SUPER_FAST_MODE_BIT = 64;
	
	
	private static final int INITIAL_BAUD_RATE = 9600;
	
	// Relative to the original byte
	private static final int FIRST_BYTE_BIT = (1 << 7);
	
	// Relative to the combined data sequence
	private static final int MISSED_TIMING_BIT = (1 << 13);
	
	private static final int INPUT_NUMBER_SHIFT = 10;
	private static final int INPUT_NUMBER_MASK = 7; // 3 bits
	
	private static final int DATA_MASK = (1023);
	
	
	protected final BlockingQueue<DataVO> queue = new ArrayBlockingQueue<DataVO>(20000);
	
	
	private volatile CommPortIdentifier portID;
	private volatile SerialPort port = null;
	private volatile InputStream in = null;
	private volatile OutputStream out = null;
	
	private volatile ListenerThread thread;
	
	private volatile boolean fastMode = false;
	private int fastModeInput = 0;
	private int[] inputs;
	
	
	
	
	
	/**
	 * Start the serial listener.  The serial device will be opened and the starting
	 * command bytes will be written to the device.
	 */
	public void startListener(ArduinoADConfiguration configuration) throws IOException, PortInUseException,
			UnsupportedCommOperationException, NoSuchPortException {
		
		this.stopListener();
		queue.clear();
		
		String name = configuration.getSerialDevice();
		boolean externalReference = configuration.getExternalReference();
		boolean fastMode = configuration.getFastMode();
		int delay = configuration.getDelay();
		int baudrate = configuration.getSerialSpeed();
		int[] inputNumbers = configuration.getInputArray();
		
		if (baudrate % 100 != 0) {
			throw new IllegalArgumentException("baud rate not multiple of 100");
		}
		
		
		int inputBits = 0;
		for (int i : configuration.getInputArray()) {
			if (i < 0 || i > INPUTS) {
				throw new IllegalArgumentException("illegal input number " + i + " specified");
			}
			inputBits |= (1 << i);
		}
		if (inputBits == 0) {
			throw new IllegalArgumentException("no inputs specified");
		}
		if (externalReference) {
			inputBits |= EXTERNAL_REFERENCE_BIT;
		}
		if (fastMode) {
			if (inputNumbers.length > 1) {
				throw new IllegalArgumentException("Fast mode possible only with one input");
			}
			inputBits |= SUPER_FAST_MODE_BIT;
			this.fastModeInput = inputNumbers[0];
		}
		this.fastMode = fastMode;
		this.inputs = configuration.getInputArray();
		
		
		if (port != null) {
			System.err.println("ERROR: open() called with port=" + port);
			Thread.dumpStack();
			close();
		}
		
		try {
			findPort(name);
			open();
			
			// Sleep in case Arduino booted on port open
			if (DEBUG)
				System.out.println("Sleeping....");
			Thread.sleep(2000);
			
			/*   Rami's arduino mega need excess booting time
			System.out.println("Sleeping....");
			Thread.sleep(5000);
			System.out.println("Sleeping....");
			Thread.sleep(5000);
			System.out.println("Sleeping....");
			Thread.sleep(5000);
			System.out.println("Sleeping....");
			Thread.sleep(5000);
			*/
			
			
			if (DEBUG)
				System.out.println("Continuing...");
			
			if (DEBUG) {
				System.out.println("");
			}
			
			// Write initial bytes
			int rate = baudrate / 100;
			out.write(inputBits & 0xFF);
			out.flush();
			out.write((delay >>> 16) & 0xFF);
			out.flush();
			out.write((delay >>> 8) & 0xFF);
			out.flush();
			out.write(delay & 0xFF);
			out.flush();
			out.write((rate >>> 8) & 0xFF);
			out.flush();
			out.write(rate & 0xFF);
			out.flush();
			
			setBaudRate(baudrate);
		} catch (NoSuchPortException e) {
			System.err.println("Serial error: " + e);
			throw e;
		} catch (IOException e) {
			System.err.println("Serial error, closing: " + e);
			close();
			throw e;
		} catch (PortInUseException e) {
			System.err.println("Serial error, closing: " + e);
			close();
			throw e;
		} catch (UnsupportedCommOperationException e) {
			System.err.println("Serial error, closing: " + e);
			close();
			throw e;
		} catch (InterruptedException e) {
			e.printStackTrace();
			close();
			return;
		}
		
		thread = new ListenerThread();
		thread.start();
	}
	
	
	/**
	 * Stops the serial listener.
	 */
	public void stopListener() {
		if (thread != null) {
			
			// Interrupt and wait for listening thread to die
			thread.interrupt();
			for (int i = 0; i < 50; i++) {
				if (thread == null)
					break;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (thread != null) {
				System.err.println("Listening thread would not shut down, closing device anyway.");
			}
		}
		
		queue.clear();
		
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public BlockingQueue<DataVO> getQueue() {
		return queue;
	}
	
	
	public boolean isListenerRunning() {
		return (thread != null);
	}
	
	
	@SuppressWarnings("unchecked")
	public String[] getSerialPorts() {
		ArrayList<String> list = new ArrayList<String>();
		
		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
			
			if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				list.add(pid.getName());
			}
		}
		
		return list.toArray(new String[0]);
	}
	
	
	
	private void findPort(String name) throws IOException, NoSuchPortException {
		this.portID = CommPortIdentifier.getPortIdentifier(name);
		
		if (DEBUG)
			System.out.println("Found port " + portID);
	}
	
	
	private void open() throws PortInUseException, UnsupportedCommOperationException,
			IOException {
		
		if (DEBUG)
			System.out.println("Opening port " + portID.getName());
		
		port = (SerialPort) portID.open("OpenRocket", 1000);
		
		port.setSerialPortParams(INITIAL_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		
		port.setInputBufferSize(SERIAL_READ_BUFFER);
		port.setOutputBufferSize(1);
		
		port.enableReceiveTimeout(10);
		if (!port.isReceiveTimeoutEnabled()) {
			System.err.println("WARNING: Receive timeout not supported by driver");
		}
		
		port.enableReceiveThreshold(1);
		if (!port.isReceiveThresholdEnabled()) {
			System.err.println("WARNING: Receive threshold not supported by driver");
		}
		
		in = port.getInputStream();
		out = port.getOutputStream();
		
		if (DEBUG)
			System.out.println("Opened successfully");
		
	}
	
	private void setBaudRate(int rate) throws UnsupportedCommOperationException {
		if (DEBUG)
			System.out.println("Setting baud rate " + rate);
		port.setSerialPortParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
	}
	
	private void close() throws IOException {
		if (DEBUG)
			System.out.println("Closing port");
		
		SerialPort p = port;
		port = null;
		in = null;
		out = null;
		
		if (p != null)
			p.close();
	}
	
	
	
	
	
	private class ListenerThread extends Thread {
		
		private int firstByte = -1;
		private DataVO dataVO = null;
		private OutputStream debugDump = null;
		
		
		public ListenerThread() {
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			
			try {
				if (DEBUG_DUMP_FILE != null) {
					System.err.println("Opening debug dump file " + DEBUG_DUMP_FILE);
					debugDump = new FileOutputStream(DEBUG_DUMP_FILE);
				}
				
				readData();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				thread = null;
				if (debugDump != null) {
					try {
						System.err.println("Closing debug dump file " + DEBUG_DUMP_FILE);
						debugDump.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
		
		private void readData() throws IOException {
			long timeStamp;
			byte[] buffer = new byte[SERIAL_READ_BUFFER];
			
			while (true) {
				// Read data
				if (DEBUG_DATA)
					System.out.println("Reading data");
				
				int count = in.read(buffer);
				timeStamp = System.nanoTime();
				
				if (debugDump != null && count > 0) {
					debugDump.write(buffer, 0, count);
				}
				
				// Check for interrupt
				if (Thread.interrupted()) {
					break;
				}
				
				// Handle data
				for (int i = 0; i < count; i++) {
					// Convert to int
					int c = buffer[i] & 0xFF;
					if (DEBUG_DATA)
						System.out.printf("Received byte 0x%02X, binary=" + binary(c) + "\n", c);
					
					handleByte(c, timeStamp);
				}
			}
		}
		
		
		private void handleByte(int c, long timeStamp) {
			
			// Handle the byte
			if (fastMode) {
				
				handleFastData(c, timeStamp);
				
			} else if ((c & FIRST_BYTE_BIT) != 0) {
				
				// First byte of sequence
				if (firstByte >= 0) {
					if (DEBUG_MISSES)
						System.err.println("Two subsequent first data bytes received");
					queue.offer(new DataVO());
				}
				firstByte = c;
				
			} else {
				
				// Second byte of sequence
				if (firstByte >= 0) {
					
					// Both bytes received
					int data = (firstByte << 7) | c;
					handleData(data, timeStamp);
					firstByte = -1;
					
				} else {
					if (DEBUG_MISSES)
						System.err.println("Second byte received without first byte");
					queue.offer(new DataVO());
				}
				
			}
		}
		
		// Super-fast 8-bit mode
		private void handleFastData(int value, long timeStamp) {
			if (DEBUG_DATA) {
				System.out.println("Firing data event: value=" + (value << 2)
						+ " input=" + fastModeInput + " in fast mode");
			}
			DataVO dataVO = new DataVO();
			dataVO.setValue(fastModeInput, value << 2);
			dataVO.setTimeStamp((timeStamp + 500) / 1000);
			queue.offer(dataVO);
		}
		
		// Normal multiplexed 10-bit mode
		private void handleData(int data, long timeStamp) {
			
			boolean timingFault = ((data & MISSED_TIMING_BIT) != 0);
			int n = ((data >>> INPUT_NUMBER_SHIFT) & INPUT_NUMBER_MASK);
			
			if (n < 0 || n >= INPUTS || Arrays.binarySearch(inputs, n) < 0) {
				if (DEBUG_MISSES)
					System.err.println("Received input n=" + n);
				queue.offer(new DataVO());
				return;
			}
			int value = (data & DATA_MASK);
			
			if (dataVO != null && dataVO.getRawValue(n) >= 0) {
				if (DEBUG_DATA)
					System.out.println("Firing data event: " + dataVO);
				queue.offer(dataVO);
				dataVO = null;
			}
			
			if (dataVO == null) {
				dataVO = new DataVO();
				dataVO.setTimeStamp((timeStamp + 500) / 1000);
			}
			dataVO.setValue(n, value);
			dataVO.addTimingFault(timingFault);
			
		}
		
	}
	
	
	private static String binary(int c) {
		StringBuffer sb = new StringBuffer(8);
		for (int i = 7; i >= 0; i--) {
			if ((c & (1 << i)) == 0)
				sb.append('0');
			else
				sb.append('1');
		}
		return sb.toString();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		SerialDataCommunicator c = new SerialDataCommunicator();
		//		Configuration config = new Configuration("/dev/ttyUSB0", 115200, 1000, false, false, 
		//				new int[]{1}, new String[]{"Input1"});
		
		System.out.println("Finding...");
		c.findPort("/dev/ttyUSB0");
		System.out.println("Opening...");
		c.open();
		
		System.out.println("Sleeping...");
		Thread.sleep(2000);
		
		System.out.println("Writing...");
		c.out.write(1);
		c.out.write(2);
		c.out.write(3);
		c.out.write(4);
		c.out.write(5);
		c.out.write(6);
		
		System.out.println("Sleeping...");
		Thread.sleep(2000);
		
		System.out.println("Setting baud rate...");
		c.setBaudRate(115200);
		
		System.out.println("Sleeping...");
		Thread.sleep(2000);
		
		System.out.println("Closing...");
		c.close();
		System.out.println("Exiting...");
		
	}
	
}
