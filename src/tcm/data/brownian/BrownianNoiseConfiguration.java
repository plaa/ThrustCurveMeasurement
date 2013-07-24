package tcm.data.brownian;

import tcm.configuration.AbstractConfiguration;

public class BrownianNoiseConfiguration extends AbstractConfiguration {
	
	public double getFrequency() {
		return getDouble("frequency", 1000);
	}
	
	public void setFrequency(double frequency) {
		map.put("frequency", frequency);
		fireChangeEvent();
	}
	
	public int getGrouping() {
		return getInt("grouping", 10);
	}
	
	public void setGrouping(int grouping) {
		map.put("grouping", grouping);
		fireChangeEvent();
	}
	
	public double getAmplitude() {
		return getDouble("amplitude", 1.0);
	}
	
	public void setAmplitude(double amplitude) {
		map.put("amplitude", amplitude);
		fireChangeEvent();
	}
	
	public double getMinimum() {
		return getDouble("min", 0);
	}
	
	public void setMinimum(double minimum) {
		map.put("min", minimum);
		fireChangeEvent();
	}
	
	public double getMaximum() {
		return getDouble("max", 1023);
	}
	
	public void setMaximum(double maximum) {
		map.put("max", maximum);
		fireChangeEvent();
	}
	
	public double getTimingMissProbability() {
		return getDouble("timingMissProbability", 0);
	}
	
	public void setTimingMissProbability(double timingMissProbability) {
		map.put("timingMissProbability", timingMissProbability);
		fireChangeEvent();
	}
	
	public double getDataErrorProbability() {
		return getDouble("dataErrorProbability", 0);
	}
	
	public void setDataErrorProbability(double dataErrorProbability) {
		map.put("dataErrorProbability", dataErrorProbability);
		fireChangeEvent();
	}
	
}
