package tcm.configuration;


public interface Configurator {
	
	/**
	 * Open a dialog to input configuration.
	 * 
	 * @param current	the current configuration, or null if none present
	 * @return			the new configuration, or null if the user canceled the action
	 */
	public Configuration configure(Configuration current);
	
}
