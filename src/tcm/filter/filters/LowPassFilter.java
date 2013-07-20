package tcm.filter.filters;

import java.awt.Component;

import net.sf.openrocket.plugin.Plugin;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;

@Plugin
public class LowPassFilter extends AbstractDataFilter {
	
	private static final double P = 0.01;
	
	@Override
	public String getName() {
		return "Low-pass filter";
	}
	
	@Override
	public String getDescription() {
		return "Apply a low-pass filter to the data.";
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		double avg = measurement.getDataPoints().get(0).getValue();
		for (DataPoint p : measurement.getDataPoints()) {
			avg = (1 - P) * avg + P * p.getValue();
			p.setValue(avg);
		}
		
		return measurement;
	}
	
	@Override
	public Measurement filterOriginalData(Measurement measurement) {
		return measurement;
	}
	
	@Override
	public Component getConfigurationComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
