package data;

public class DataPoint {
	
	private long time;
	private double value;
	private long timestamp;
	
	public DataPoint() {
		
	}
	
	public DataPoint(long time, double value, long timestamp) {
		this.time = time;
		this.value = value;
		this.timestamp = timestamp;
	}
	
	/**
	 * The microsecond time value for the data point.  This is the time when the
	 * data point was measured, from the start of the measurement.
	 * 
	 * It may differ from the time when the point was received.
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * Set the microsecond time value for the data point.
	 */
	public void setTime(long time) {
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
	
	
	@Override
	public String toString() {
		return "DataPoint [time=" + time + ", value=" + value + ", timestamp=" + timestamp + "]";
	}
	
}
