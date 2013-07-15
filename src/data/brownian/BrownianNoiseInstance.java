package data.brownian;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;

import net.sf.openrocket.util.MathUtil;
import data.DataPoint;
import data.MeasurementInstance;
import data.MeasurementListener;

public class BrownianNoiseInstance implements MeasurementInstance, ActionListener {
	
	private List<MeasurementListener> listeners = new ArrayList<MeasurementListener>();
	private Timer timer;
	
	private double time = 0;
	private double current;
	private double min, max;
	
	private double dataErrorProbability;
	private double timingMissProbability;
	
	private Random rnd = new Random();
	
	private double delay;
	private int pointsPerGroup;
	private double amplitude;
	
	public BrownianNoiseInstance(BrownianNoiseConfiguration config) {
		double hertz = config.getFrequency();
		pointsPerGroup = config.getGrouping();
		amplitude = config.getAmplitude();
		
		dataErrorProbability = config.getDataErrorProbability();
		timingMissProbability = config.getTimingMissProbability();
		
		delay = 1.0 / hertz;
		
		timer = new Timer((int) (delay * pointsPerGroup * 1000), this);
		current = 0;
		min = config.getMinimum();
		max = config.getMaximum();
	}
	
	@Override
	public void start() throws IOException {
		timer.start();
	}
	
	@Override
	public void stop() {
		timer.stop();
	}
	
	@Override
	public void addListener(MeasurementListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(MeasurementListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		List<DataPoint> points = new ArrayList<DataPoint>(pointsPerGroup);
		for (int i = 0; i < pointsPerGroup; i++) {
			current += rnd.nextGaussian() * amplitude;
			current = MathUtil.clamp(current, min, max);
			
			DataPoint dp = new DataPoint(time, current, System.currentTimeMillis());
			points.add(dp);
			time += delay;
		}
		
		for (MeasurementListener l : listeners) {
			l.processData(points);
		}
		
		if (rnd.nextDouble() < timingMissProbability) {
			for (MeasurementListener l : listeners) {
				l.timingMiss();
			}
		}
		if (rnd.nextDouble() < dataErrorProbability) {
			for (MeasurementListener l : listeners) {
				l.dataError();
			}
		}
	}
	
	@Override
	public double getMinimunValue() {
		return min;
	}
	
	@Override
	public double getMaximumValue() {
		return max;
	}
	
}
