package tcm.defaults;

import java.io.File;
import java.util.EventObject;
import java.util.prefs.Preferences;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.util.StateChangeListener;
import tcm.gui.adaptors.DoubleValue;

import com.google.inject.Singleton;

@Singleton
public class Defaults {
	
	private Preferences prefs = Preferences.userRoot().node("ThrustCurveMeasurement");
	
	public void remember(DoubleValue value, String key) {
		remember(new DoubleModel(value, "Value"), key);
	}
	
	public void remember(final DoubleModel model, final String key) {
		remember(model, key, true);
	}
	
	public void remember(final DoubleModel model, final String key, boolean recall) {
		if (recall) {
			double value = getDouble(key, Double.NaN);
			if (!Double.isNaN(value)) {
				model.setValue(value);
			}
		}
		
		model.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				putDouble(key, model.getValue());
			}
		});
	}
	
	
	public void remember(final IntegerModel model, final String key) {
		remember(model, key, true);
	}
	
	public void remember(final IntegerModel model, final String key, boolean recall) {
		if (recall) {
			int value = getInt(key, Integer.MIN_VALUE);
			if (value != Integer.MIN_VALUE) {
				model.setValue(value);
			}
		}
		
		model.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				putInt(key, model.getValue());
			}
		});
	}
	
	
	public double getDouble(String key, double def) {
		return prefs.getDouble(key, def);
	}
	
	public void putDouble(String key, double value) {
		prefs.putDouble(key, value);
	}
	
	public int getInt(String key, int def) {
		return prefs.getInt(key, def);
	}
	
	public void putInt(String key, int value) {
		prefs.putInt(key, value);
	}
	
	public String getString(String key, String def) {
		return prefs.get(key, def);
	}
	
	public void putString(String key, String value) {
		prefs.put(key, value);
	}
	
	public File getFile(String key, File def) {
		String file = prefs.get(key, null);
		if (file == null) {
			return def;
		} else {
			return new File(file);
		}
	}
	
	public void putFile(String key, File value) {
		prefs.put(key, value.getAbsolutePath());
	}
	
}
