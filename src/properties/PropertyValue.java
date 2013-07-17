package properties;

/**
 * A class that represents a specific property value.  It contains
 * the value, and the type.
 */
public class PropertyValue {
	
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
	
	
	
	
	private void check(Object v) {
		if (!type.getTypeClass().isInstance(v)) {
			throw new IllegalArgumentException("Invalid class provided, type class=" + type.getTypeClass() + " object=" +
					(v == null ? "null" : v.getClass()));
		}
	}
	
}
