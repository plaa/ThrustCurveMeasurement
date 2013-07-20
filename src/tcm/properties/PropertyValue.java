package tcm.properties;

import tcm.util.Copyable;

/**
 * A class that represents a specific property value.  It contains
 * the value, and the type.
 */
public class PropertyValue implements Copyable<PropertyValue>, Cloneable {
	
	private final PropertyType type;
	private Object value;
	
	
	
	public PropertyValue(PropertyType type, Object value) {
		this.type = type;
		check(value);
		this.value = value;
	}
	
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		check(value);
		this.value = value;
	}
	
	public PropertyType getType() {
		return type;
	}
	
	
	@Override
	public PropertyValue copy() {
		try {
			PropertyValue copy = (PropertyValue) this.clone();
			if (copy.value instanceof Copyable) {
				copy.value = ((Copyable<?>) copy.value).copy();
			}
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	private void check(Object v) {
		if (!type.getTypeClass().isInstance(v)) {
			throw new IllegalArgumentException("Invalid class provided, type class=" + type.getTypeClass() + " object=" +
					(v == null ? "null" : v.getClass()));
		}
	}
	
	
}
