package tcm.file.xml;

import net.sf.openrocket.util.AbstractChangeSource;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.thoughtworks.xstream.XStream;

public class TCMXStreamProvider implements Provider<XStream> {
	
	@Inject
	private MeasurementDocumentConverter measurementDocumentConverter;
	@Inject
	private DataFilterConverter dataFilterConverter;
	@Inject
	private PropertyListConverter propertyListConverter;
	
	@Override
	public XStream get() {
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
