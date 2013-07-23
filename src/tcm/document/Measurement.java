package tcm.document;

import java.util.ArrayList;
import java.util.List;

import tcm.calibration.Calibration;
import tcm.data.DataPoint;
import tcm.properties.PropertyList;
import tcm.util.Copyable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Data contained about a data measurement.
 */
public class Measurement implements Copyable<Measurement> {
	
	@XStreamAlias("DataPoints")
	private final ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
	@XStreamAlias("PropertyList")
	private PropertyList propertyList = new PropertyList();
	@XStreamAlias("Calibration")
	private Calibration calibration = new Calibration();
	
	
	
	public List<DataPoint> getDataPoints() {
		return dataPoints;
	}
	
	public PropertyList getPropertyList() {
		return propertyList;
	}
	
	public void setPropertyList(PropertyList propertyList) {
		this.propertyList = propertyList;
	}
	
	public Calibration getCalibration() {
		return calibration;
	}
	
	public void setCalibration(Calibration calibration) {
		this.calibration = calibration;
	}
	
	
	public Measurement copy() {
		Measurement copy = new Measurement();
		
		copy.dataPoints.ensureCapacity(dataPoints.size());
		for (DataPoint p : dataPoints) {
			copy.dataPoints.add(p.copy());
		}
		
		copy.propertyList = this.propertyList.copy();
		copy.calibration = this.calibration.copy();
		
		return copy;
	}
}
