package tcm.filter;

import java.awt.Component;

import net.sf.openrocket.util.AbstractChangeSource;
import tcm.document.Measurement;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Helper class for implementing data filters.
 * 
 * You need to extend this class and implement the missing methods.
 * The class must contain a public no-arguments constructor.
 */
public abstract class AbstractDataFilter extends AbstractChangeSource implements DataFilterPlugin, DataFilter {
	
	@Inject
	private Injector injector;
	
	
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
		return injector.getInstance(this.getClass());
	}
	
}
