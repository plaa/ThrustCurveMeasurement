package tcm.document;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.StateChangeListener;
import tcm.filter.DataFilter;
import tcm.filter.filters.LowPassFilter;
import tcm.util.ChangeSourceList;

public class MeasurementDocument extends AbstractChangeSource implements StateChangeListener {
	
	private Measurement measurement = new Measurement();
	
	private final ChangeSourceList<DataFilter> filter;
	
	public MeasurementDocument() {
		filter = new ChangeSourceList<DataFilter>(new ArrayList<DataFilter>());
		filter.addChangeListener(this);
		
		filter.add(new LowPassFilter());
	}
	
	
	public Measurement getMeasurement() {
		return measurement;
	}
	
	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}
	
	public List<DataFilter> getFilters() {
		return filter;
	}
	
	@Override
	public void stateChanged(EventObject o) {
		fireChangeEvent();
	}
}
