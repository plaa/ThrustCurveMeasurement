package tcm.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.Measurement;

import com.google.inject.Inject;
import com.google.inject.Provider;

@Plugin
public class CSVExporter implements FileExporter {
	
	@Inject
	private Provider<CSVExportDialog> dialog;
	
	@Override
	public String getName() {
		return "Comma-separated value (CSV) files (*.csv, *.txt)";
	}
	
	@Override
	public List<String> getExtensions() {
		return Arrays.asList("csv", "txt");
	}
	
	@Override
	public boolean export(File file, Measurement measurement) throws IOException {
		return dialog.get().export(file, measurement);
	}
	
}
