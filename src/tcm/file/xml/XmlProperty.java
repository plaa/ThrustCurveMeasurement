package tcm.file.xml;

import tcm.properties.PropertyType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Property")
public class XmlProperty {
	
	public String name;
	public Class<? extends PropertyType> type;
	public Object value;
	
}
