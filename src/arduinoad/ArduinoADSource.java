package arduinoad;

import net.sf.openrocket.plugin.Plugin;
import configuration.Configuration;
import configuration.Configurator;
import data.MeasurementInstance;
import data.MeasurementSource;

@Plugin
public class ArduinoADSource implements MeasurementSource {
	
	
	@Override
	public String getName() {
		return "ArduinoAD";
	}
	
	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public MeasurementInstance getInstance(Configuration configuration) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
