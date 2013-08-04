package tcm.data.arduinoad;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Random;

import net.sf.openrocket.util.MathUtil;

public class MockDataCommunicator extends SerialDataCommunicator {
	
	private static final int MISSED_BYTE_PORTION = 10000;
	
	private int delay;
	private int[] inputs;
	private MockReaderThread thread;
	
	@Override
	public void startListener(ArduinoADConfiguration configuration)
			throws IOException, PortInUseException, UnsupportedCommOperationException {
		
		if (thread != null)
			stopListener();
		
		this.delay = configuration.getDelay();
		this.inputs = new int[] { configuration.getInput() };
		
		thread = new MockReaderThread();
		thread.start();
	}
	
	
	@Override
	public boolean isListenerRunning() {
		if (thread == null)
			return false;
		return thread.isAlive();
	}
	
	
	@Override
	public void stopListener() {
		if (thread != null)
			thread.interrupt();
		thread = null;
	}
	
	
	
	@Override
	public String[] getSerialPorts() {
		return new String[] { "/dev/ttyUSB0", "/dev/mock" };
	}
	
	
	
	
	
	private class MockReaderThread extends Thread {
		
		public MockReaderThread() {
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			Random rnd = new Random();
			
			long time = System.nanoTime();
			while (true) {
				time += delay * 1000;
				boolean delaying = false;
				while (System.nanoTime() < time)
					delaying = true;
				
				if (Thread.interrupted())
					break;
				
				if (!delaying) {
					//					System.out.println("Missed timing");
					time = System.nanoTime();
				}
				//				try {
				//					Thread.sleep(delay/1000, (delay%1000)*1000);
				//				} catch (InterruptedException e) {
				//					break;
				//				}
				
				if (rnd.nextInt(MISSED_BYTE_PORTION) == 0)
					queue.offer(new DataVO());
				
				DataVO data = new DataVO();
				data.setTimeStamp(System.nanoTime() / 1000);
				data.addTimingFault(!delaying);
				for (int n : inputs) {
					double value = 100 * (1 + 0.1 * rnd.nextGaussian()) * (n + 1);
					data.setValue(n, MathUtil.clamp((int) value, 0, 1023));
				}
				queue.offer(data);
			}
			
			System.err.println("Mock thread exiting");
		}
	}
	
	
}
