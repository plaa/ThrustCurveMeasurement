package seriallistener;

import java.util.Arrays;

public class DataVO implements Cloneable {
	
	private long timeStamp = -1;
	private boolean timingFault = false;
	private int[] rawValues;
	private double[] filteredValues;
	
	/**
	 * Sole constructor.  Sets all data values to illegal values.
	 */
	public DataVO() {
		rawValues = new int[SerialDataCommunicator.INPUTS];
		Arrays.fill(rawValues, -1);
		filteredValues = new double[SerialDataCommunicator.INPUTS];
		Arrays.fill(filteredValues, Double.NaN);
	}
	
	
	/**
	 * Whether this object represents a byte miss.  Returns true iff all
	 * raw values are negative.
	 * 
	 * @return	whether this object represents a data miss object.
	 */
	public boolean isByteMissObject() {
		for (int value : rawValues) {
			if (value >= 0)
				return false;
		}
		return true;
	}
	
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	public boolean isTimingFault() {
		return timingFault;
	}
	
	public void setTimingFault(boolean timingFault) {
		this.timingFault = timingFault;
	}
	
	public void addTimingFault(boolean timingFault) {
		this.timingFault |= timingFault;
	}
	
	
	/**
	 * Set both the raw and filtered value.
	 */
	public void setValue(int n, int value) {
		this.rawValues[n] = value;
		if (value < 0)
			this.filteredValues[n] = Double.NaN;
		else
			this.filteredValues[n] = value;
	}
	
	
	public void setRawValue(int n, int value) {
		this.rawValues[n] = value;
	}
	
	public int getRawValue(int n) {
		return this.rawValues[n];
	}
	
	
	public void setFilteredValue(int n, double value) {
		this.filteredValues[n] = value;
	}
	
	public double getFilteredValue(int n) {
		return this.filteredValues[n];
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DataVO");
		sb.append("[rawValues=");
		sb.append(Arrays.toString(rawValues));
		sb.append(",timeStamp=");
		sb.append(timeStamp);
		if (timingFault)
			sb.append(",timingFault");
		sb.append(']');
		return sb.toString();
	}
	
	
	@Override
	public DataVO clone() {
		try {
			DataVO copy = (DataVO) super.clone();
			copy.rawValues = this.rawValues.clone();
			copy.filteredValues = this.filteredValues.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
