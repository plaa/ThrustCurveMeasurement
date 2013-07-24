package tcm.filter;

import java.awt.Component;
import java.util.Map;

import net.sf.openrocket.util.ChangeSource;
import tcm.document.Measurement;

public interface DataFilter extends ChangeSource {
	
	public String getName();
	
	public String getDescription();
	
	public Measurement filter(Measurement measurement);
	
	public Measurement filterOriginalData(Measurement measurement);
	
	public Map<String, Object> getConfiguration();
	
	public void setConfiguration(Map<String, Object> config);
	
	public Component getConfigurationComponent();
	
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
	
}
