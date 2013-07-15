package arduinoad;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import configuration.Configuration;
import data.DataPoint;
import data.MeasurementInstance;
import data.MeasurementListener;

public class ArduinoADInstance implements MeasurementInstance {
	
	private final ArduinoConfiguration config;
	private final SerialDataCommunicator communicator;
	
	private final List<MeasurementListener> listeners = new ArrayList<MeasurementListener>();
	
	private Timer timer;
	
	public ArduinoADInstance(Configuration configuration) {
		this.config = (ArduinoConfiguration) configuration;
		this.communicator = new SerialDataCommunicator();
		
		this.timer = new Timer(200, new BackgroundListener());
	}
	
	
	@Override
	public void addListener(MeasurementListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void start() throws IOException {
		timer.start();
		try {
			communicator.startListener(config);
		} catch (PortInUseException e) {
			throw new IOException("Unable to open port", e);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("Unable to open port", e);
		} catch (NoSuchPortException e) {
			throw new IOException("Unable to open port", e);
		}
	}
	
	@Override
	public void stop() {
		communicator.stopListener();
		timer.stop();
	}
	
	private class BackgroundListener implements ActionListener {
		
		private LinkedList<DataVO> datavos = new LinkedList<DataVO>();
		private List<DataPoint> datapoints = new LinkedList<DataPoint>();
		
		private int count = 0;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			communicator.getQueue().drainTo(datavos);
			while (!datavos.isEmpty()) {
				DataVO vo = datavos.removeFirst();
				count++;
				if (vo.isTimingFault()) {
					flush();
					fireTimingFault();
				}
				if (vo.isByteMissObject()) {
					flush();
					fireDataError();
				}
				
				DataPoint point = convert(vo);
				if (point != null) {
					datapoints.add(point);
				}
			}
			flush();
		}
		
		private void flush() {
			if (!datapoints.isEmpty()) {
				for (MeasurementListener l : listeners) {
					l.processData(datapoints);
				}
			}
			datapoints.clear();
		}
		
		private void fireDataError() {
			for (MeasurementListener l : listeners) {
				l.dataError();
			}
		}
		
		private void fireTimingFault() {
			for (MeasurementListener l : listeners) {
				l.timingMiss();
			}
		}
		
		private DataPoint convert(DataVO vo) {
			DataPoint point = new DataPoint();
			boolean set = false;
			for (int i = 0; i < 6; i++) {
				int value = vo.getRawValue(i);
				if (value >= 0) {
					point.setValue(value);
					set = true;
					break;
				}
			}
			
			if (!set) {
				return null;
			}
			
			point.setTimestamp(vo.getTimeStamp());
			point.setTime(count * ((long) config.getDelay()));
			return point;
		}
	}
	
	@Override
	public double getMinimunValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	@Override
	public double getMaximumValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
