package tcm.configuration;

import java.util.Map;

import tcm.util.Copyable;

public interface Configuration extends Copyable<Configuration> {
	
	/**
	 * Return a map reflecting the current configuration.  This may be
	 * backed by the actual configuration or be separate.
	 * <p>
	 * The map must only contain immutable basic Java types that can be natively
	 * serialized by XStream (e.g. primitives, String, Date, etc).
	 * 
	 * @return a map representing the configuration
	 */
	public Map<String, Object> getMap();
	
	
	/**
	 * Update the configuration based on the values in the provided map.
	 * The map may be a partial or whole configuration.  Old values may be retained
	 * in the configuration if new values are not provided.
	 * 
	 * @param map	a map representing the configuration
	 */
	public void updateMap(Map<String, Object> map);
	
	
}
