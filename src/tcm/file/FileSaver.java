package tcm.file;

import java.io.File;
import java.io.IOException;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.MeasurementDocument;

@Plugin
public interface FileSaver extends FilePlugin {
	
	/**
	 * Save to the specified file.  Overwrite if the file exists.
	 * This may open application-modal dialogs as part of the saving process.
	 * 
	 * @param file	the file to save to
	 * @param doc	the document to save
	 * @return		true when successful, false if the user canceled the action
	 * @throws		IOException if a problem occurs while saving the file
	 */
	public boolean save(File file, MeasurementDocument doc) throws IOException;
	
}
