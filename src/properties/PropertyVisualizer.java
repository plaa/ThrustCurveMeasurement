package properties;

import java.awt.Color;
import java.awt.Component;

/**
 * A visualizer for a property value.  Typically a table cell renderer or editor.
 */
public interface PropertyVisualizer {
	
	public Component getComponent(PropertyValue value, Color foreground, Color background);
	
}
