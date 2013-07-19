package properties;

import java.awt.Color;
import java.awt.Component;

/**
 * A renderer for a property value.  Typically a table cell renderer.
 */
public interface PropertyRenderer {
	
	/**
	 * Return a component to render the value.  For performance reasons, this should
	 * return the same component, changed to display the new value.
	 * 
	 * @param value			the value to display
	 * @param foreground	suggested foreground color (may be ignored)
	 * @param background	suggested background color (may be ignored)
	 * @return				a component to render the value
	 */
	public Component getRenderer(PropertyValue value, Color foreground, Color background);
	
}
