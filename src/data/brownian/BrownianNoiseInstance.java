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
	
	private Random rnd = new Random();
	
	private double delay;
	private int pointsPerGroup;
	
	public BrownianNoiseInstance(BrownianNoiseConfiguration config) {
		int hertz = 1000;
		pointsPerGroup = 10;
		
		delay = 1.0 / hertz;
		
		timer = new Timer((int) (delay * pointsPerGroup * 1000), this);
		current = 0;
		min = 0;
		max = 1024;
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
	public void actionPerformed(ActionEvent e) {
		List<DataPoint> points = new ArrayList<DataPoint>(pointsPerGroup);
		for (int i = 0; i < pointsPerGroup; i++) {
			current += rnd.nextGaussian();
			current = MathUtil.clamp(current, min, max);
			
			DataPoint dp = new DataPoint((long) (time * 1000), current, System.currentTimeMillis());
			points.add(dp);
			time += delay;
		}
		
		for (MeasurementListener l : listeners) {
			l.processData(points);
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
