package tcm.filter;

import java.awt.Component;

import net.sf.openrocket.util.AbstractChangeSource;
import tcm.document.Measurement;

/**
 * Helper class for implementing data filters.
 * 
 * You need to extend this class and implement the missing methods.
 * The class must contain a public no-arguments constructor.
 */
public abstract class AbstractDataFilter extends AbstractChangeSource implements DataFilterPlugin, DataFilter {
	
	/**
	 * Does nothing; returns the measurement as-is.  This is the case
	 * for most data filters.
	 */
	@Override
	public Measurement filterOriginalData(Measurement measurement) {
		return measurement;
	}
	
	
	/**
	 * Returns null.  Override this method to provide a configuration component.
	 */
	@Override
	public Component getConfigurationComponent() {
		return null;
	}
	
	@Override
	public DataFilter getInstance() {
		try {
			return this.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to create new instance of " + this.getClass() +
					"\nClass must have a public no-arguments constructor.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to create new instance of " + this.getClass() +
					"\nClass must have a public no-arguments constructor.", e);
		}
	}
	
}
