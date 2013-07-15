package data.brownian;

import net.sf.openrocket.plugin.Plugin;
import configuration.Configuration;
import configuration.Configurator;
import data.MeasurementInstance;
import data.MeasurementSource;

@Plugin
public class BrownNoiseMeasurementSource implements MeasurementSource {
	
	@Override
	public String getName() {
		return "Brownian noise (testing)";
	}
	
	@Override
	public Configurator getConfigurator() {
		return new BrownianNoiseConfigurator();
	}
	
	@Override
	public MeasurementInstance getInstance(Configuration configuration) {
		return new BrownianNoiseInstance((BrownianNoiseConfiguration) configuration);
	}
	
}
