package tcm.file.xml;

import java.util.ArrayList;

import tcm.document.Measurement;
import tcm.filter.DataFilter;

import com.thoughtworks.xstream.annotations.XStreamAlias;


public class XmlMeasurementDocument {
	
	@XStreamAlias("Measurement")
	public Measurement measurement = new Measurement();
	
	@XStreamAlias("Filters")
	public ArrayList<DataFilter> filters;
	
}
