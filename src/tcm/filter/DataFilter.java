package tcm.filter;

import java.awt.Component;

import net.sf.openrocket.util.ChangeSource;
import tcm.document.Measurement;

public interface DataFilter extends ChangeSource {
	
	public String getName();
	
	public String getDescription();
	
	public Measurement filter(Measurement measurement);
	
	public Measurement filterOriginalData(Measurement measurement);
	
	public Component getConfigurationComponent();
	
}
