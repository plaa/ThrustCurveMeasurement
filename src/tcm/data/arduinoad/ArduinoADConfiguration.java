package tcm.data.arduinoad;

import tcm.configuration.AbstractConfiguration;


public class ArduinoADConfiguration extends AbstractConfiguration {
	
	
	public String getSerialDevice() {
		return getString("serialDevice", "");
	}
	
	public void setSerialDevice(String serialDevice) {
		map.put("serialDevice", serialDevice);
		fireChangeEvent();
	}
	
	public int getSerialSpeed() {
		return getInt("serialSpeed", 115200);
	}
	
	public void setSerialSpeed(int serialSpeed) {
		map.put("serialSpeed", serialSpeed);
		fireChangeEvent();
	}
	
	public int getDelay() {
		return getInt("delay", 1000);
	}
	
	public void setDelay(int delay) {
		map.put("delay", delay);
		fireChangeEvent();
	}
	
	public boolean isExternalReference() {
		return getBoolean("externalReference", false);
	}
	
	public void setExternalReference(boolean externalReference) {
		map.put("externalReference", externalReference);
		fireChangeEvent();
	}
	
	public int getInput() {
		return getInt("input", 0);
	}
	
	public void setInput(int input) {
		map.put("input", input);
		fireChangeEvent();
	}
	
	public boolean isFastMode() {
		return false;
	}
	
}
