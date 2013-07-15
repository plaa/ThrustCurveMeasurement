package filter;

import java.util.List;

import arduinoad.AbstractDataSource;
import arduinoad.DataListener;
import arduinoad.DataVO;


public abstract class DataFilter extends AbstractDataSource implements DataListener {

	private int sampleCount = 0;
	private int byteMissCount = 0;
	private int timingMissCount = 0;
	
	
	
	/**
	 * The current byte miss count.  This value is increased every time a byte
	 * miss event is received.
	 */
	public int getByteMissCount() {
		return byteMissCount;
	}

	/**
	 * Set the current byte miss count.
	 */
	public void setByteMissCount(int byteMissCount) {
		this.byteMissCount = byteMissCount;
	}
	
	
	
	public int getSampleCount() {
		return sampleCount;
	}
	
	public void setSampleCount(int count) {
		this.sampleCount = count;
	}
	
	
	public int getTimingMissCount() {
		return timingMissCount;
	}

	public void setTimingMissCount(int timingMissCount) {
		this.timingMissCount = timingMissCount;
	}

	
	public void reset() {
		this.timingMissCount = 0;
		this.sampleCount = 0;
		this.byteMissCount = 0;
	}
	
	@Override
	public void processData(final List<DataVO> data) {
	
		for (DataVO d: data) {
			if (d.isByteMissObject())
				byteMissCount++;
			else
				sampleCount++;
			if (d.isTimingFault())
				timingMissCount++;
			filter(d);
		}
		fireData(data);
		
	}
	
	
	
	protected abstract void filter(DataVO data);
	
}
