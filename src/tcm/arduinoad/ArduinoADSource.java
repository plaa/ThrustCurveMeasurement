package tcm.arduinoad;

import tcm.configuration.Configuration;
import tcm.configuration.Configurator;
import tcm.data.MeasurementInstance;
import tcm.data.MeasurementSource;

//@Plugin
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
