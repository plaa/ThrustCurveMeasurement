package tcm.file.xml;

import java.util.ArrayList;
import java.util.List;

import tcm.properties.PropertyList;
import tcm.properties.PropertyValue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PropertyListConverter implements Converter {
	
	@Inject
	private Injector injector;
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class cls) {
		return PropertyList.class.isAssignableFrom(cls);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		PropertyList propertyList = (PropertyList) obj;
		List<XmlProperty> list = new ArrayList<XmlProperty>();
		
		for (int i = 0; i < propertyList.size(); i++) {
			XmlProperty p = new XmlProperty();
			p.name = propertyList.getName(i);
			PropertyValue value = propertyList.getValue(i);
			p.type = value.getType().getClass();
			p.value = value.getValue();
			list.add(p);
		}
		context.convertAnother(list);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		PropertyList list = new PropertyList();
		List<XmlProperty> xml = (List<XmlProperty>) context.convertAnother(list, ArrayList.class);
		for (XmlProperty p : xml) {
			list.insert(p.name, new PropertyValue(injector.getInstance(p.type), p.value));
		}
		
		return list;
	}
	
}
