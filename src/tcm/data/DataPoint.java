package tcm.data;

import net.sf.openrocket.util.MathUtil;
import tcm.util.Copyable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("DataPoint")
public class DataPoint implements Cloneable, Copyable<DataPoint> {
	
	@XStreamAsAttribute
	private double time;
	@XStreamAsAttribute
	private double value;
	@XStreamAsAttribute
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataPoint other = (DataPoint) obj;
		if (!MathUtil.equals(this.time, other.time))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (!MathUtil.equals(this.value, other.value))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((int) (time * 1000));
		result = prime * result + ((int) (timestamp));
		result = prime * result + ((int) (value * 1000));
		return result;
	}
	
}
