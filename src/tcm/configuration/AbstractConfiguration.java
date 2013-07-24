package tcm.configuration;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.util.AbstractChangeSource;

/**
 * An implementation of Configuration that is backed by the map returned by getMap().
 * Subclasses can add setters/getters for specific properties that utilize the map.
 */
public class AbstractConfiguration extends AbstractChangeSource implements Configuration, Cloneable {
	
	protected Map<String, Object> map = new HashMap<String, Object>();
	
	@Override
	public Map<String, Object> getMap() {
		return map;
	}
	
	
	
	protected int getInt(String key, int def) {
		Object value = map.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			return def;
		}
	}
	
	protected double getDouble(String key, double def) {
		Object value = map.get(key);
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return def;
		}
	}
	
	protected boolean getBoolean(String key, boolean def) {
		Object value = map.get(key);
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return def;
		}
	}
	
	protected String getString(String key, String def) {
		Object value = map.get(key);
		if (value instanceof String) {
			return (String) value;
		} else {
			return def;
		}
	}
	
	
	@Override
	public Configuration copy() {
		try {
			AbstractConfiguration copy = (AbstractConfiguration) clone();
			copy.map = new HashMap<String, Object>(this.map);
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
