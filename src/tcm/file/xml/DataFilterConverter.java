package tcm.file.xml;

import java.util.HashMap;
import java.util.Map;

import tcm.filter.DataFilter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DataFilterConverter implements Converter {
	
	@Inject
	private Injector injector;
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz) {
		return DataFilter.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		DataFilter filter = (DataFilter) obj;
		writer.startNode("enabled");
		context.convertAnother(filter.isEnabled());
		writer.endNode();
		
		Map<String, Object> config = filter.getConfiguration();
		if (config != null && !config.isEmpty()) {
			writer.startNode("Configuration");
			context.convertAnother(config);
			writer.endNode();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		DataFilter filter;
		
		String className = reader.getNodeName();
		try {
			filter = (DataFilter) injector.getInstance(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new ConversionException("Could not find data filter for class name " + className);
		}
		
		Map<String, Object> config = new HashMap<String, Object>();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals("enabled")) {
				Boolean b = (Boolean) context.convertAnother(config, Boolean.class);
				if (b != null) {
					filter.setEnabled(b);
				}
			} else if (reader.getNodeName().equals("Configuration")) {
				config = (Map<String, Object>) context.convertAnother(config, HashMap.class);
				filter.setConfiguration(config);
			}
			reader.moveUp();
		}
		
		return filter;
	}
	
}