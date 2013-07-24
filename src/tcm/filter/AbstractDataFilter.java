package tcm.filter;

import java.awt.Component;
import java.util.Collections;
import java.util.Map;

import net.sf.openrocket.util.AbstractChangeSource;
import tcm.configuration.AbstractConfiguration;
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
	
	protected AbstractConfiguration configuration;
	
	/**
	 * Does nothing; returns the measurement as-is.  This is the case
	 * for most data filters.
	 */
	@Override
	public Measurement filterOriginalData(Measurement measurement) {
		return measurement;
	}
	
	
	@Override
	public Map<String, Object> getConfiguration() {
		return Collections.unmodifiableMap(configuration.getMap());
	}
	
	@Override
	public void setConfiguration(Map<String, Object> config) {
		configuration.updateMap(config);
		fireChangeEvent();
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
