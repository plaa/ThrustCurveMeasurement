package properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of key-name-value property triplets.  The key is the logical
 * key for the property, the name is the human-readable name or label,
 * and the value is the provided value of the property.
 */
public class PropertyList {
	
	private final List<String> keys = new ArrayList<String>();
	private final Map<String, PropertyValue> values = new HashMap<String, PropertyValue>();
	private final Map<String, String> names = new HashMap<String, String>();
	
	
	
	public void insert(String key, String name, PropertyValue value) {
		insert(key, name, value, keys.size());
	}
	
	public void insert(String key, String name, PropertyValue value, int position) {
		keys.add(position, key);
		names.put(key, name);
		values.put(key, value);
	}
	
	public void setValue(String key, PropertyValue value) {
		if (!values.containsKey(key)) {
			throw new IllegalArgumentException("Key " + key + " not in PropertyList, you must insert the key first");
		}
		values.put(key, value);
	}
	
	public PropertyValue getValue(String key) {
		return values.get(key);
	}
	
	public PropertyValue getValue(int index) {
		return values.get(keys.get(index));
	}
	
	public String getName(String key) {
		return names.get(key);
	}
	
	public String getName(int index) {
		return names.get(keys.get(index));
	}
	
	public int size() {
		return keys.size();
	}
	
}
