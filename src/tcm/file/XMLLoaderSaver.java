package tcm.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.util.AbstractChangeSource;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;
import tcm.file.xml.DataFilterConverter;
import tcm.file.xml.MeasurementDocumentConverter;
import tcm.file.xml.PropertyListConverter;
import tcm.file.xml.XmlMeasurementDocument;
import tcm.file.xml.XmlProperty;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

@Plugin
public class XMLLoaderSaver implements FileSaver, FileLoader {
	
	@Inject
	private MeasurementDocumentConverter measurementDocumentConverter;
	@Inject
	private DataFilterConverter dataFilterConverter;
	@Inject
	private PropertyListConverter propertyListConverter;
	
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
	public MeasurementDocument load(File file) throws IOException {
		XStream xs = getXStream();
		return (MeasurementDocument) xs.fromXML(file);
	}
	
	@Override
	public boolean save(File file, MeasurementDocument document) throws IOException {
		XStream xs = getXStream();
		
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			xs.toXML(document, writer);
			return true;
		} finally {
			writer.close();
		}
	}
	
	
	
	private XStream getXStream() {
		XStream xs = new XStream();
		xs.processAnnotations(MeasurementDocument.class);
		xs.processAnnotations(Measurement.class);
		xs.processAnnotations(DataPoint.class);
		xs.processAnnotations(XmlMeasurementDocument.class);
		xs.processAnnotations(XmlProperty.class);
		
		xs.registerConverter(measurementDocumentConverter);
		xs.registerConverter(dataFilterConverter);
		xs.registerConverter(propertyListConverter);
		
		xs.omitField(AbstractChangeSource.class, "listeners");
		return xs;
	}
	
}
