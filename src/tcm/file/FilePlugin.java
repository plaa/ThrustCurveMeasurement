package tcm.file;

import java.util.List;

public interface FilePlugin {
	
	/**
	 * Return the name of the file type this plugin supports.
	 */
	public String getName();
	
	/**
	 * Return the file extensions this plugin supports (without dots).
	 */
	public List<String> getExtensions();
	
	
}
