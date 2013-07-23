package tcm.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Plugin;
import tcm.document.MeasurementDocument;
import tcm.file.xml.TCMXStreamProvider;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

@Plugin
public class XMLLoader implements FileLoader {
	
	@Inject
	TCMXStreamProvider provider;
	
	@Override
	public String getName() {
		return "Thrust curve measurement files (*.tcm, *.xml)";
	}
	
	@Override
	public List<String> getExtensions() {
		return Arrays.asList("tcm", "xml");
	}
	
	@Override
	public MeasurementDocument load(File file) throws IOException {
		XStream xs = provider.get();
		return (MeasurementDocument) xs.fromXML(file);
	}
	
}
