
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Buggy {
	private static final boolean DEBUG = true;

	private static final int INITIAL_BAUD_RATE = 9600;
	

	private volatile CommPortIdentifier portID;
	private volatile SerialPort port = null;
	private volatile InputStream in = null;
	private volatile OutputStream out = null;
	
	
	
	private void findPort(String name) throws IOException, NoSuchPortException {
		this.portID = CommPortIdentifier.getPortIdentifier(name);
		
		if (DEBUG)
			System.out.println("Found port "+portID);
	}
	
	
	private void open() throws PortInUseException, UnsupportedCommOperationException,
	IOException {

		if (DEBUG)
			System.out.println("Opening port "+portID.getName());
		
		port = (SerialPort)portID.open("OpenRocket",1000);
		
		port.setSerialPortParams(INITIAL_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, 
				SerialPort.PARITY_NONE);

		port.setInputBufferSize(1);
		port.setOutputBufferSize(1);
		
		port.enableReceiveTimeout(500);
		
		in = port.getInputStream();
		out = port.getOutputStream();
		
		if (DEBUG)
			System.out.println("Opened successfully");
		
	}
	
	private void setBaudRate(int rate) throws UnsupportedCommOperationException {
		System.out.println("Setting baud rate "+rate);
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
	
	
	
	public static void main(String[] args) throws Exception {
		
		Buggy c = new Buggy();
		
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
