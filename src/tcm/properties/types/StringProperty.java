package tcm.properties.types;

import java.awt.Component;

import javax.swing.JTextField;

import net.sf.openrocket.plugin.Plugin;
import tcm.properties.PropertyEditor;
import tcm.properties.PropertyRenderer;
import tcm.properties.PropertyType;
import tcm.properties.PropertyValue;

/**
 * A single line string.
 */
@Plugin
public class StringProperty implements PropertyType {
	
	private PropertyRenderer renderer = new ToStringPropertyRenderer();
	private Editor editor = new Editor();
	
	@Override
	public String getName() {
		return "string";
	}
	
	@Override
	public Class<?> getTypeClass() {
		return String.class;
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
			return field.getText();
		}
		
	}
	
	
	
}
