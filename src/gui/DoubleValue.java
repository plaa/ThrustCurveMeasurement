package gui;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;

import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.StateChangeListener;

public class DoubleValue implements ChangeSource {
	
	private final ArrayList<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	private double value;
	
	
	public DoubleValue() {
		
	}
	
	public DoubleValue(double value) {
		this.value = value;
	}
	
	
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
		fireChange();
	}
	
	
	private void fireChange() {
		StateChangeListener[] array = listeners.toArray(new StateChangeListener[0]);
		ChangeEvent e = new ChangeEvent(this);
		for (StateChangeListener l : array) {
			l.stateChanged(e);
		}
	}
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
}
