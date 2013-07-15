package filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import arduinoad.DataVO;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;

public class DataAveragingFilter extends DataFilter {
	
	private static final Pair<Double, Double> NO_DATA =
			new Pair<Double, Double>(Double.NaN, Double.NaN);
	
	/** Maximum age of data to store, in microseconds. */
	private final long maxAge;
	
	private final LinkedList<DataVO> oldData = new LinkedList<DataVO>();
	
	private long startTime;
	
	
	public DataAveragingFilter(long maxAge) {
		this.maxAge = maxAge;
		this.startTime = System.currentTimeMillis();
	}
	
	

	@Override
	public void reset() {
		super.reset();
		oldData.clear();
		this.startTime = System.currentTimeMillis();
	}
	
	
	public DataVO getLatest() {
		return oldData.getFirst().clone();
	}
	
	public int getBufferSize() {
		return oldData.size();
	}
	
	public long getTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	
	/**
	 * Return the average and standard deviation of the filtered values of input
	 * n from the latest time microseconds.
	 * 
	 * @param n		the input number.
	 * @param time	the filtering time, in microseconds.
	 * @return		(average,stddev) from that time frame (NaN is not applicable)
	 */
	public Pair<Double, Double> averageFilteredValues(int n, long time) {
		double average, stddev;
		int count;
		
		if (oldData.isEmpty()) {
			return NO_DATA;
		}
		if (time <= 0) {
			return new Pair<Double, Double>(oldData.getFirst().getFilteredValue(n), Double.NaN);
		}
		
		long oldestData = oldData.getFirst().getTimeStamp() - time;
		
		// Calculate average
		average = 0;
		count = 0;
		for (DataVO data : oldData) {
			if (data.getTimeStamp() < oldestData)
				break;
			if (!Double.isNaN(data.getFilteredValue(n))) {
				average += data.getFilteredValue(n);
				count++;
			}
		}
		
		if (count == 0) {
			return NO_DATA;
		}
		average /= count;
		
		// Calculate standard deviation
		if (count == 1) {
			return new Pair<Double, Double>(average, Double.NaN);
		}
		
		stddev = 0;
		for (DataVO data : oldData) {
			if (data.getTimeStamp() < oldestData)
				break;
			if (!Double.isNaN(data.getFilteredValue(n))) {
				stddev += MathUtil.pow2(average - data.getFilteredValue(n));
			}
		}
		
		stddev /= count - 1;
		stddev = Math.sqrt(stddev);
		
		return new Pair<Double, Double>(average, stddev);
	}
	
	

	/**
	 * Return the average and standard deviation of the raw values of input
	 * n from the latest time microseconds.
	 * 
	 * @param n		the input number.
	 * @param time	the filtering time, in microseconds.
	 * @return		(average,stddev) from that time frame (NaN is not applicable)
	 */
	public Pair<Double, Double> averageRawValues(int n, long time) {
		double average, stddev;
		int count;
		
		if (oldData.isEmpty()) {
			return NO_DATA;
		}
		if (time <= 0) {
			return new Pair<Double, Double>((double) oldData.getFirst().getRawValue(n), Double.NaN);
		}
		long oldestData = oldData.getFirst().getTimeStamp() - time;
		
		// Calculate average
		average = 0;
		count = 0;
		for (DataVO data : oldData) {
			if (data.getTimeStamp() < oldestData)
				break;
			if (data.getRawValue(n) >= 0) {
				average += data.getRawValue(n);
				count++;
			}
		}
		
		if (count == 0) {
			return NO_DATA;
		}
		average /= count;
		
		// Calculate standard deviation
		if (count == 1) {
			return new Pair<Double, Double>(average, Double.NaN);
		}
		
		stddev = 0;
		for (DataVO data : oldData) {
			if (data.getTimeStamp() < oldestData)
				break;
			if (data.getRawValue(n) >= 0) {
				stddev += MathUtil.pow2(average - data.getRawValue(n));
			}
		}
		
		stddev /= count - 1;
		stddev = Math.sqrt(stddev);
		
		return new Pair<Double, Double>(average, stddev);
	}
	
	



	/**
	 * Return the average of the data interval from the latest "time" microseconds.
	 * 
	 * @param time	the filtering time, in microseconds.
	 * @return		average from that time frame in microseconds (NaN is not applicable)
	 */
	public double averageInterval(final long time) {
		if (oldData.size() < 2)
			return Double.NaN;
		
		if (time <= 0) {
			return oldData.get(0).getTimeStamp() - oldData.get(1).getTimeStamp();
		}
		
		long oldestData = oldData.getFirst().getTimeStamp() - time;
		
		count = 0;
		Iterator<DataVO> iterator = oldData.iterator();
		DataVO first = iterator.next();
		DataVO previous = first;
		while (iterator.hasNext()) {
			DataVO next = iterator.next();
			if (next.getTimeStamp() < oldestData) {
				break;
			}
			previous = next;
			count++;
		}
		
		return ((double) first.getTimeStamp() - previous.getTimeStamp()) / count;
	}
	
	
	/**
	 * Return an unmodifiable view of the currently buffered data. 
	 * @return	a view of the buffered data, newest data first.
	 */
	public List<DataVO> getBufferedData() {
		return Collections.unmodifiableList(oldData);
	}
	
	


	private int count = 0;
	
	@Override
	protected void filter(DataVO data) {
		if (data.isByteMissObject())
			return;
		
		oldData.addFirst(data.clone());
		
		count++;
		if (count > 5000) {
			count = 0;
			// Remove old data
			while (oldData.getLast().getTimeStamp() < data.getTimeStamp() - maxAge) {
				oldData.removeLast();
			}
		}
	}
	
}
