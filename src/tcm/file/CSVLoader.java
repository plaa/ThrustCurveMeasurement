package tcm.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.MeasurementDocument;

import com.google.inject.Inject;
import com.google.inject.Provider;

@Plugin
public class CSVLoader implements FileLoader {
	
	@Inject
	private Provider<CSVOpenDialog> dialog;
	
	@Override
	public String getName() {
		return "Comma-separated value (CSV) files (*.csv, *.txt)";
	}
	
	@Override
	public List<String> getExtensions() {
		return Arrays.asList("csv", "txt");
	}
	
	@Override
	public MeasurementDocument load(File file) throws IOException {
		return dialog.get().open(file);
	}
	
}
