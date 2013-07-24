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
	
}
