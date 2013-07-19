package tcm.properties.types;

import java.awt.Component;

import javax.swing.JTextField;

import net.sf.openrocket.plugin.Plugin;
import tcm.properties.PropertyEditor;
import tcm.properties.PropertyRenderer;
import tcm.properties.PropertyType;
import tcm.properties.PropertyValue;

@Plugin
public class IntegerProperty implements PropertyType {
	
	private PropertyRenderer renderer = new ToStringPropertyRenderer();
	private Editor editor = new Editor();
	
	@Override
	public String getName() {
		return "integer";
	}
	
	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}
	
	@Override
	public PropertyRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public PropertyEditor getEditor() {
		return editor;
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
				return Integer.parseInt(field.getText());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
	}
	
}
