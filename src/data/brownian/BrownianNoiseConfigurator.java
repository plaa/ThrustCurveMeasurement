package data.brownian;

import configuration.Configuration;
import configuration.Configurator;

public class BrownianNoiseConfigurator implements Configurator {
	
	@Override
	public Configuration configure(Configuration current) {
		return new BrownianNoiseConfiguration();
	}
	
	@Override
	public String store(Configuration config) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Configuration load(String stored) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
