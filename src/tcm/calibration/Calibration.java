package tcm.calibration;

import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.MathUtil;
import tcm.util.Copyable;

public class Calibration extends AbstractChangeSource implements Copyable<Calibration>, Cloneable {
	
	private double inputValue1 = 0;
	private double outputValue1 = 0;
	private double inputValue2 = 1;
	private double outputValue2 = 1;
	
	/**
	 * Convert an input value to an output value.
	 */
	public double toOutput(double input) {
		try {
			return MathUtil.map(input, inputValue1, inputValue2, outputValue1, outputValue2);
		} catch (IllegalArgumentException e) {
			return Double.NaN;
		}
	}
	
	/**
	 * Convert an output value to an input value.
	 */
	public double toInput(double output) {
		try {
			return MathUtil.map(output, outputValue1, outputValue2, inputValue1, inputValue2);
		} catch (IllegalArgumentException e) {
			return Double.NaN;
		}
	}
	
	
	
	
	public double getInputValue1() {
		return inputValue1;
	}
	
	public void setInputValue1(double inputValue1) {
		this.inputValue1 = inputValue1;
		fireChangeEvent();
	}
	
	public double getOutputValue1() {
		return outputValue1;
	}
	
	public void setOutputValue1(double outputValue1) {
		this.outputValue1 = outputValue1;
		fireChangeEvent();
	}
	
	public double getInputValue2() {
		return inputValue2;
	}
	
	public void setInputValue2(double inputValue2) {
		this.inputValue2 = inputValue2;
		fireChangeEvent();
	}
	
	public double getOutputValue2() {
		return outputValue2;
	}
	
	public void setOutputValue2(double outputValue2) {
		this.outputValue2 = outputValue2;
		fireChangeEvent();
	}
	
	@Override
	public String toString() {
		return "Calibration [" + inputValue1 + " -> " + outputValue1 + ", " + inputValue2 + " -> " + outputValue2 + "]";
	}
	
	@Override
	public Calibration copy() {
		try {
			return (Calibration) this.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
