package tcm.filter;

import tcm.document.Measurement;

/**
 * Helper class for implementing data filters.
 * 
 * You need to extend this class and implement the missing methods.
 * The class must contain a public no-arguments constructor.
 */
public abstract class AbstractDataFilter implements DataFilterPlugin, DataFilter {
	
	/**
	 * Does nothing; returns the measurement as-is.  This is the case
	 * for most data filters.
	 */
	public Measurement filterOriginalData(Measurement measurement) {
		return measurement;
	}
	
	
	@Override
	public DataFilter getInstance() {
		try {
			return this.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to create new instance of " + this.getClass() +
					"\nClass must have a public no-arguments constructor.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to create new instance of " + this.getClass() +
					"\nClass must have a public no-arguments constructor.", e);
		}
	}
	
}
