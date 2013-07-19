package tcm.data.brownian;

import tcm.configuration.Configuration;
import tcm.configuration.Configurator;
import tcm.data.MeasurementInstance;
import tcm.data.MeasurementSource;
import net.sf.openrocket.plugin.Plugin;

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
