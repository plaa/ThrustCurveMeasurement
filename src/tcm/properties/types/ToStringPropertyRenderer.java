package tcm.properties.types;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;

import tcm.properties.PropertyRenderer;
import tcm.properties.PropertyValue;

/**
 * A PropertyRenderer that renders the value's toString output as a JLabel.
 */
public class ToStringPropertyRenderer implements PropertyRenderer {
	private JLabel label = new JLabel();
	
	@Override
	public Component getRenderer(PropertyValue value, Color foreground, Color background) {
		label.setText(value.getValue().toString());
		label.setForeground(foreground);
		label.setBackground(background);
		label.setOpaque(true);
		return label;
	}
	
}
