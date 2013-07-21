package tcm.gui.adaptors;

import net.sf.openrocket.util.AbstractChangeSource;

public class IntegerValue extends AbstractChangeSource {
	
	private int value;
	
	public IntegerValue() {
		this(0);
	}
	
	public IntegerValue(int value) {
		this.value = value;
	}
	
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		if (this.value == value)
			return;
		this.value = value;
		fireChangeEvent();
	}
	
}
