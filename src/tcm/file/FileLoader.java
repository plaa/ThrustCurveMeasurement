package tcm.file;

import java.io.File;
import java.io.IOException;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.MeasurementDocument;

@Plugin
public interface FileLoader extends FilePlugin {
	
	/**
	 * Load the specified file.  This may open application-modal dialogs as part
	 * of the loading process.
	 * 
	 * @param file	the file to load
	 * @return		the loaded document, or null if the user cancelled the operation
	 * @throws		IOException if a problem occurs while loading the file
	 */
	public MeasurementDocument load(File file) throws IOException;
	
}
