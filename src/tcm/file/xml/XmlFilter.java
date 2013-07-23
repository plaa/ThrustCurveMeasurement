package tcm.file.xml;

import tcm.configuration.Configuration;
import tcm.filter.DataFilter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Filter")
public class XmlFilter {
	
	public Class<? extends DataFilter> filter;
	public Configuration configuration;
	
}
