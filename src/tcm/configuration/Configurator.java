package tcm.configuration;


public interface Configurator {
	
	/**
	 * Open a dialog to input configuration.
	 * 
	 * @param current	the current configuration, or null if none present
	 * @return			the new configuration, or null if the user canceled the action
	 */
	public Configuration configure(Configuration current);
	
	/**
	 * Store a configuration into a string.
	 * 
	 * @param config	the configuration
	 * @return			a string representing the configuration
	 */
	public String store(Configuration config);
	
	/**
	 * Load a configuration from a string.
	 * 
	 * @param stored	the stored configuration
	 * @return			the configuration
	 */
	public Configuration load(String stored);
}
