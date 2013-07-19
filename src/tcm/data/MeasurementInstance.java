package tcm.data;

import java.io.IOException;

/**
 * An instance that produces measurement data.
 */
public interface MeasurementInstance {
	
	public void start() throws IOException;
	
	public void stop();
	
	public double getMinimunValue();
	
	public double getMaximumValue();
	
	public void addListener(MeasurementListener listener);
	
	public void removeListener(MeasurementListener listener);
	
}
