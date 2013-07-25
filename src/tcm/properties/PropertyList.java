package tcm.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tcm.util.Copyable;

/**
 * A list of name-value property pairs.  The name functions as both the
 * user-displayed name and key for fetching the value.  When used as a key,
 * the methods are lenient, and are for example case-insensitive.
 */
public class PropertyList implements Copyable<PropertyList> {
	
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
	
	
	@Override
	public PropertyList copy() {
		PropertyList copy = new PropertyList();
		
		copy.keys.addAll(this.keys);
		copy.names.putAll(this.names);
		
		for (Entry<String, PropertyValue> entry : this.values.entrySet()) {
			copy.values.put(entry.getKey(), entry.getValue().copy());
		}
		
		return copy;
	}
	
	
	private String normalize(String name) {
		return name.toLowerCase().replaceAll("\\s+", " ").trim();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyList other = (PropertyList) obj;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}
	
}
