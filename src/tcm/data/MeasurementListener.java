package tcm.data;

import java.util.List;

public interface MeasurementListener {
	
	/**
	 * Process data from the measurement source.
	 * 
	 * @param data			a list of data items that have arrived since the last call, in order.
	 */
	public void processData(List<DataPoint> data);
	
	/**
	 * Called when the measurement source detects that a measurement was missed.
	 */
	public void timingMiss();
	
	/**
	 * Called when the measurement source detect a data error in the stream.
	 */
	public void dataError();
	
}
