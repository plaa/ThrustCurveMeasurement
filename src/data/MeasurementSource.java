package data;

import configuration.Configuration;
import configuration.Configurator;
import net.sf.openrocket.plugin.Plugin;

@Plugin
public interface MeasurementSource {
	
	/**
	 * Return the name of this measurement source.
	 */
	public String getName();
	
	/**
	 * Return the configurator for this plugin.
	 */
	public Configurator getConfigurator();
	
	/**
	 * Get a MeasurementInstance with a specific configuration.
	 */
	public MeasurementInstance getInstance(Configuration configuration);
	
}
