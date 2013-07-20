package tcm.filter.filters;

import java.awt.Component;

import net.sf.openrocket.plugin.Plugin;
import tcm.calibration.Calibration;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;

@Plugin
public class CalibrationFilter extends AbstractDataFilter {
	
	@Override
	public String getName() {
		return "Apply calibration";
	}
	
	@Override
	public String getDescription() {
		return "Apply the calibration to the data.";
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		Calibration calibration = measurement.getCalibration();
		for (DataPoint p : measurement.getDataPoints()) {
			p.setValue(calibration.toOutput(p.getValue()));
		}
		return measurement;
	}
	
	@Override
	public Measurement filterOriginalData(Measurement measurement) {
		return filter(measurement);
	}
	
	@Override
	public Component getConfigurationComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
