package properties.types;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;

import net.sf.openrocket.plugin.Plugin;
import properties.PropertyType;
import properties.PropertyValue;
import properties.PropertyVisualizer;

/**
 * A single line string.
 */
@Plugin
public class StringProperty implements PropertyType {
	
	private Renderer renderer = new Renderer();
	
	@Override
	public String getName() {
		return "String";
	}
	
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
	
	@Override
	public PropertyVisualizer getRenderer() {
		return renderer;
	}
	
	@Override
	public PropertyVisualizer getEditor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private class Renderer implements PropertyVisualizer {
		private JLabel label = new JLabel();
		
		@Override
		public Component getComponent(PropertyValue value, Color foreground, Color background) {
			label.setText(value.getValue().toString());
			label.setForeground(foreground);
			label.setBackground(background);
			label.setOpaque(true);
			return label;
		}
		
	}
	
	
	
}
