package tcm.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.MeasurementDocument;
import tcm.file.xml.TCMXStreamProvider;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

@Plugin
public class XMLSaver implements FileSaver {
	
	@Inject
	TCMXStreamProvider provider;
	
	public void save(MeasurementDocument document) {
	}
	
	@Override
	public String getName() {
		return "Thrust curve measurement files (*.tcm)";
	}
	
	@Override
	public List<String> getExtensions() {
		return Arrays.asList("tcm", "xml");
	}
	
	@Override
	public boolean save(File file, MeasurementDocument document) throws IOException {
		XStream xs = provider.get();
		
		FileWriter writer = new FileWriter(file);
		try {
			xs.toXML(document, writer);
			return true;
		} finally {
			writer.close();
		}
	}
	
}
