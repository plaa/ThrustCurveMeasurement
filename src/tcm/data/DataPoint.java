package tcm.data;

import tcm.util.Copyable;

public class DataPoint implements Cloneable, Copyable<DataPoint> {
	
	private double time;
	private double value;
	private long timestamp;
	
	public DataPoint() {
		
	}
	
	public DataPoint(double time, double value, long timestamp) {
		this.time = time;
		this.value = value;
		this.timestamp = timestamp;
	}
	
	/**
	 * The time value for the data point in seconds.  This is the time when the
	 * data point was measured, from the start of the measurement.
	 * 
	 * It may differ from the time when the point was received.
	 */
	public double getTime() {
		return time;
	}
	
	/**
	 * Set the time value for the data point in seconds.
	 */
	public void setTime(double time) {
		this.time = time;
	}
	
	
	/**
	 * The millisecond timestamp when the data point was received, from Unix epoch.
	 * This may deviate from the time value, if there is delays between the measurement
	 * and transfer.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	/**
	 * The data point value.
	 */
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	
	public DataPoint copy() {
		try {
			return (DataPoint) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return "DataPoint [time=" + time + ", value=" + value + ", timestamp=" + timestamp + "]";
	}
	
}
