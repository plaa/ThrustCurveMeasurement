package tcm.filter;

import java.awt.Component;

import tcm.document.Measurement;

public interface DataFilter {
	
	public String getName();
	
	public String getDescription();
	
	public Measurement filter(Measurement measurement);
	
	public Measurement filterOriginalData(Measurement measurement);
	
	public Component getConfigurationComponent();
	
}
