package data.brownian;

import net.sf.openrocket.util.AbstractChangeSource;
import configuration.Configuration;

public class BrownianNoiseConfiguration extends AbstractChangeSource implements Configuration, Cloneable {
	
	private double frequency = 1000;
	private int grouping = 10;
	private double amplitude = 1;
	private double minimum = 0;
	private double maximum = 1023;
	private double timingMissProbability = 0;
	private double dataErrorProbability = 0;
	
	
	public double getFrequency() {
		return frequency;
	}
	
	public void setFrequency(double frequency) {
		this.frequency = frequency;
		fireChangeEvent();
	}
	
	
	public int getGrouping() {
		return grouping;
	}
	
	public void setGrouping(int grouping) {
		this.grouping = grouping;
		fireChangeEvent();
	}
	
	
	
	public double getAmplitude() {
		return amplitude;
	}
	
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
		fireChangeEvent();
	}
	
	
	
	
	public double getMinimum() {
		return minimum;
	}
	
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	
	public double getMaximum() {
		return maximum;
	}
	
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	
	public double getTimingMissProbability() {
		return timingMissProbability;
	}
	
	public void setTimingMissProbability(double timingMissProbability) {
		this.timingMissProbability = timingMissProbability;
		fireChangeEvent();
	}
	
	
	public double getDataErrorProbability() {
		return dataErrorProbability;
	}
	
	public void setDataErrorProbability(double dataErrorProbability) {
		this.dataErrorProbability = dataErrorProbability;
		fireChangeEvent();
	}
	
	@Override
	public Configuration copy() {
		try {
			return (Configuration) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
