package seriallistener;

import java.util.List;

public interface DataListener {

	/**
	 * Process data from the source.
	 * 
	 * @param data			a list of data items that have arrived since the last call, in order.
	 */
	public void processData(List<DataVO> data);
	
	
}
