package tcm.data.arduinoad;

import net.sf.openrocket.plugin.Plugin;
import tcm.configuration.Configuration;
import tcm.configuration.Configurator;
import tcm.data.MeasurementInstance;
import tcm.data.MeasurementSource;

import com.google.inject.Inject;
import com.google.inject.Provider;

@Plugin
public class ArduinoADSource implements MeasurementSource {
	
	@Inject
	private Provider<ArduinoADConfigurator> configurator;
	
	@Override
	public String getName() {
		return "ArduinoAD";
	}
	
	@Override
	public Configurator getConfigurator() {
		return configurator.get();
	}
	
	@Override
	public MeasurementInstance getInstance(Configuration configuration) {
		return new ArduinoADInstance(configuration);
	}
	
}
