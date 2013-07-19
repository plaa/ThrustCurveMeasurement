package properties;

import java.awt.Component;

/**
 * An editor for a property value.  Typically a table cell editor.
 */
public interface PropertyEditor {
	
	/**
	 * Return a component to edit the value.  For performance reasons, this should
	 * return the same component, changed to display the new value.
	 * 
	 * @param value			the value to edit
	 * @return				a component to edit the value
	 */
	public Component getEditor(PropertyValue value);
	
	/**
	 * Return the current value of the editor.  This is the raw value, not a PropertyValue object.
	 * May return null for an invalid value / not set.
	 */
	public Object getCurrentValue();
	
}
