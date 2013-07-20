package tcm.filter;

import net.sf.openrocket.plugin.Plugin;

/**
 * A plugin that defines a data filter.
 */
@Plugin
public interface DataFilterPlugin {
	
	public String getName();
	
	public DataFilter getInstance();
	
}
