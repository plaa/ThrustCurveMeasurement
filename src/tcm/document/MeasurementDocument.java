package tcm.document;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.StateChangeListener;
import tcm.filter.DataFilter;
import tcm.util.ChangeSourceList;
import tcm.util.Copyable;

public class MeasurementDocument extends AbstractChangeSource implements StateChangeListener, Copyable<MeasurementDocument> {
	
	private Measurement measurement = new Measurement();
	
	private final ChangeSourceList<DataFilter> filters;
	
	public MeasurementDocument() {
		filters = new ChangeSourceList<DataFilter>(new ArrayList<DataFilter>(), true);
		filters.addChangeListener(this);
	}
	
	
	public Measurement getMeasurement() {
		return measurement;
	}
	
	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}
	
	public List<DataFilter> getFilters() {
		return filters;
	}
	
	@Override
	public void stateChanged(EventObject o) {
		fireChangeEvent();
	}
	
	
	@Override
	public MeasurementDocument copy() {
		MeasurementDocument copy = new MeasurementDocument();
		copy.measurement = this.measurement.copy();
		// TODO:  Filter configurations should be deep-copied
		copy.filters.clear();
		copy.filters.addAll(this.filters);
		return copy;
	}
}
