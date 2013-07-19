package tcm.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of name-value property pairs.  The name functions as both the
 * user-displayed name and key for fetching the value.  When used as a key,
 * the methods are lenient, and are for example case-insensitive.
 */
public class PropertyList {
	
	/*
	 * Internally:
	 *   key   - normalized name
	 *   value - the value
	 *   name  - human readable (original) form
	 */
	
	private final List<String> keys = new ArrayList<String>();
	private final Map<String, PropertyValue> values = new HashMap<String, PropertyValue>();
	private final Map<String, String> names = new HashMap<String, String>();
	
	
	
	public void insert(String name, PropertyValue value) {
		insert(name, value, keys.size());
	}
	
	public void insert(String name, PropertyValue value, int position) {
		String key = normalize(name);
		keys.add(position, key);
		names.put(key, name);
		values.put(key, value);
	}
	
	
	
	public PropertyValue getValue(String name) {
		return values.get(name);
	}
	
	public PropertyValue getValue(int index) {
		return values.get(keys.get(index));
	}
	
	public void setValue(String name, Object value) {
		PropertyValue propertyValue = values.get(name);
		if (propertyValue == null) {
			throw new IllegalArgumentException("Key " + name + " not in PropertyList, you must insert the key first");
		}
		propertyValue.setValue(value);
	}
	
	public void setValue(int index, Object value) {
		values.get(keys.get(index)).setValue(value);
	}
	
	public String getName(String name) {
		return names.get(name);
	}
	
	public String getName(int index) {
		return names.get(keys.get(index));
	}
	
	public int size() {
		return keys.size();
	}
	
	
	
	private String normalize(String name) {
		return name.toLowerCase().replaceAll("\\s+", " ").trim();
	}
}
