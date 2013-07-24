package tcm.file;

import java.io.File;
import java.io.IOException;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.Measurement;

@Plugin
public interface FileExporter extends FilePlugin {
	
	/**
	 * Export to the specified file.  Overwrite if the file exists.
	 * This may open application-modal dialogs as part of the exporting process.
	 * 
	 * @param file			the file to export to
	 * @param measurement	the (filtered) measurement to export
	 * @return				true when successful, false if the user canceled the action
	 * @throws				IOException if a problem occurs while exporting the file
	 */
	public boolean export(File file, Measurement measurement) throws IOException;
	
}
