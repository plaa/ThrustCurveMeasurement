package tcm.properties;

import net.sf.openrocket.plugin.Plugin;

/**
 * A plugin interface that defines a specific type of property,
 * e.g. string, multiline string, integer, etc.
 * 
 * Instances of this class must be immutable.
 */
@Plugin
public interface PropertyType {
	
	/**
	 * Return the name of the type, e.g. "integer", "multiline string" etc.
	 */
	public String getName();
	
	/**
	 * Return the class of the type values.
	 */
	public Class<?> getTypeClass();
	
	/**
	 * Return a PropertyRenderer for the type.
	 */
	public PropertyRenderer getRenderer();
	
	/**
	 * Return a PropertyEditor for the type.
	 */
	public PropertyEditor getEditor();
	
}
