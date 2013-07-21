package tcm.defaults;

import java.util.EventObject;
import java.util.prefs.Preferences;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.util.StateChangeListener;

import com.google.inject.Singleton;

@Singleton
public class Defaults {
	
	public static final int INT_DEFAULT = Integer.MIN_VALUE;
	
	private Preferences prefs = Preferences.userRoot().node("ThrustCurveMeasurement");
	
	public void remember(final DoubleModel model, final String key) {
		remember(model, key, true);
	}
	
	public void remember(final DoubleModel model, final String key, boolean recall) {
		if (recall) {
			double value = getDouble(key);
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
			int value = getInt(key);
			if (value != INT_DEFAULT) {
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
	
	
	public double getDouble(String key) {
		return prefs.getDouble(key, Double.NaN);
	}
	
	public void putDouble(String key, double value) {
		prefs.putDouble(key, value);
	}
	
	public int getInt(String key) {
		return prefs.getInt(key, INT_DEFAULT);
	}
	
	public void putInt(String key, int value) {
		prefs.putInt(key, value);
	}
	
}
