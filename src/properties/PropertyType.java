package properties;

import net.sf.openrocket.plugin.Plugin;

/**
 * A plugin interface that defines a specific type of property,
 * e.g. string, multiline string, integer, etc.
 */
@Plugin
public interface PropertyType {
	
	public String getName();
	
	public Class<?> getTypeClass();
	
	public PropertyVisualizer getRenderer();
	
	public PropertyVisualizer getEditor();
	
}
