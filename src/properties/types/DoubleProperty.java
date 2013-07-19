package properties.types;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.openrocket.plugin.Plugin;
import properties.PropertyEditor;
import properties.PropertyRenderer;
import properties.PropertyType;
import properties.PropertyValue;

@Plugin
public class DoubleProperty implements PropertyType {
	
	private PropertyRenderer renderer = new Renderer();
	private Editor editor = new Editor();
	
	@Override
	public String getName() {
		return "decimal number";
	}
	
	@Override
	public Class<?> getTypeClass() {
		return Double.class;
	}
	
	@Override
	public PropertyRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public PropertyEditor getEditor() {
		return editor;
	}
	
	
	private class Renderer implements PropertyRenderer {
		private JLabel label = new JLabel();
		
		@Override
		public Component getRenderer(PropertyValue value, Color foreground, Color background) {
			double v = (Double) value.getValue();
			v = Math.round(v * 10000) / 10000.0;
			label.setText("" + v);
			label.setForeground(foreground);
			label.setBackground(background);
			label.setOpaque(true);
			return label;
		}
		
	}
	
	
	private class Editor implements PropertyEditor {
		private JTextField field = new JTextField();
		
		@Override
		public Component getEditor(PropertyValue value) {
			field.setText(value.getValue().toString());
			field.setEditable(true);
			field.setBorder(null);
			return field;
		}
		
		@Override
		public Object getCurrentValue() {
			try {
				return Double.parseDouble(field.getText());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
	}
	
}
