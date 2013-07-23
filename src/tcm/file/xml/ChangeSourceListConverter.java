package tcm.file.xml;

import java.util.ArrayList;
import java.util.List;

import tcm.util.ChangeSourceList;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ChangeSourceListConverter implements Converter {
	
	// FIXME:  This probably is unnecessary and wrong
	
	@Override
	public boolean canConvert(Class cls) {
		return cls.equals(ChangeSourceList.class);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		ChangeSourceList csl = (ChangeSourceList<?>) obj;
		List list = new ArrayList(csl);
		context.convertAnother(list);
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		List list = (List) context.convertAnother(null, ArrayList.class);
		return new ChangeSourceList(list, true);
	}
	
}
