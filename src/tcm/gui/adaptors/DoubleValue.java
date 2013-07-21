package tcm.gui.adaptors;

import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.MathUtil;

public class DoubleValue extends AbstractChangeSource {
	
	private double value;
	
	public DoubleValue() {
		this(0);
	}
	
	public DoubleValue(double value) {
		this.value = value;
	}
	
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		if (MathUtil.equals(this.value, value))
			return;
		this.value = value;
		fireChangeEvent();
	}
	
}
