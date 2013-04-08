package seriallistener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable {
	
	private final String serialDevice;
	private final int serialSpeed;
	private final int delay;
	private final boolean externalReference;
	private final boolean fastMode;
	private final int[] inputs;
	private final String[] inputNames;
	
	
	public Configuration(String serialDevice, int serialSpeed, int delay,
			boolean externalReference, boolean fastMode, int[] inputNumbers,
			String[] inputNames) {
		
		if (inputNumbers.length < 1) {
			throw new IllegalArgumentException("At least one input needs to be defined");
		}
		if (inputNumbers.length != inputNames.length) {
			throw new IllegalArgumentException("inputs and names have different length");
		}
		
		this.serialDevice = serialDevice;
		this.serialSpeed = serialSpeed;
		this.delay = delay;
		this.inputs = inputNumbers.clone();
		this.inputNames = inputNames.clone();
		
		// sort
		boolean modified;
		do {
			modified = false;
			for (int i = 0; i < inputs.length - 1; i++) {
				if (inputs[i] > inputs[i + 1]) {
					int tmp = inputs[i + 1];
					inputs[i + 1] = inputs[i];
					inputs[i] = tmp;
					
					String str = inputNames[i + 1];
					inputNames[i + 1] = inputNames[i];
					inputNames[i] = str;
					modified = true;
					break;
				}
			}
		} while (modified);
		
		this.externalReference = externalReference;
		this.fastMode = fastMode;
	}
	
	public String getSerialDevice() {
		return serialDevice;
	}
	
	public int getSerialSpeed() {
		return serialSpeed;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public boolean getExternalReference() {
		return externalReference;
	}
	
	public boolean getFastMode() {
		return fastMode;
	}
	
	public List<Integer> getInputs() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i : inputs) {
			list.add(i);
		}
		return list;
	}
	
	public int[] getInputArray() {
		return inputs.clone();
	}
	
	public List<String> getInputNames() {
		List<String> list = new ArrayList<String>();
		for (String s : inputNames) {
			list.add(s);
		}
		return list;
	}
	
	public String[] getInputNamesArray() {
		return inputNames.clone();
	}
	
}
