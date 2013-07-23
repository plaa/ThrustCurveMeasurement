package tcm.file.xml;

import java.util.ArrayList;

import tcm.document.MeasurementDocument;
import tcm.filter.DataFilter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MeasurementDocumentConverter implements Converter {
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class cls) {
		return cls.equals(MeasurementDocument.class);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		MeasurementDocument doc = (MeasurementDocument) obj;
		XmlMeasurementDocument xml = new XmlMeasurementDocument();
		xml.measurement = doc.getMeasurement();
		xml.filters = new ArrayList<DataFilter>(doc.getFilters());
		context.convertAnother(xml);
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MeasurementDocument doc = new MeasurementDocument();
		XmlMeasurementDocument xml = (XmlMeasurementDocument) context.convertAnother(context, XmlMeasurementDocument.class);
		doc.setMeasurement(xml.measurement);
		doc.getFilters().addAll(xml.filters);
		return doc;
	}
	
}
