package tcm.filter.filters;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.StateChangeListener;
import tcm.configuration.CloneableConfiguration;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;

@Plugin
public class MovingMedianFilter extends AbstractDataFilter {
	
	private DoubleModel kernelLength = new DoubleModel(0.1, UnitGroup.UNITS_SHORT_TIME, 0);
	
	public MovingMedianFilter() {
		kernelLength.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				fireChangeEvent();
			}
		});
	}
	
	@Override
	public String getName() {
		return "Moving median filter";
	}
	
	@Override
	public String getDescription() {
		return "Apply a moving median filter to the data.\n" +
				"A moving average filter is optimal if the noise is normally distributed, while a " +
				"moving median filter may be better if the signal has a disproportionate amount of " +
				"large errors.";
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("length", kernelLength.getValue());
		return map;
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		List<Double> medianPoints = new ArrayList<Double>();
		
		int last = 0;
		int next = 0;
		double delta = kernelLength.getValue() / 2;
		
		List<DataPoint> points = measurement.getDataPoints();
		int n = points.size();
		double result[] = new double[n];
		for (int i = 0; i < n; i++) {
			DataPoint p = points.get(i);
			double tMin = p.getTime() - delta;
			double tMax = p.getTime() + delta;
			while (next < n && points.get(next).getTime() <= tMax) {
				double value = points.get(next).getValue();
				int index = Collections.binarySearch(medianPoints, value);
				if (index >= 0) {
					medianPoints.add(index, value);
				} else {
					medianPoints.add(-(index + 1), value);
				}
				next++;
			}
			while (last < n && points.get(last).getTime() < tMin) {
				double value = points.get(last).getValue();
				int index = Collections.binarySearch(medianPoints, value);
				if (index >= 0) {
					medianPoints.remove(index);
				} else {
					System.err.println("ERROR:  Could not find value " + value + " in median values " + medianPoints);
				}
				last++;
			}
			
			int size = medianPoints.size();
			if (size % 2 == 0) {
				result[i] = (medianPoints.get(size / 2 - 1) + medianPoints.get(size / 2)) / 2;
			} else {
				result[i] = medianPoints.get(size / 2);
			}
		}
		
		for (int i = 0; i < n; i++) {
			points.get(i).setValue(result[i]);
		}
		
		return measurement;
	}
	
	@Override
	public Component getConfigurationComponent() {
		JPanel panel = new JPanel(new MigLayout(""));
		
		panel.add(new JLabel("Length:"));
		
		JSpinner spin = new JSpinner(kernelLength.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 60lp");
		
		UnitSelector unit = new UnitSelector(kernelLength);
		panel.add(unit);
		
		BasicSlider slider = new BasicSlider(kernelLength.getSliderModel(0.0, 0.25));
		panel.add(slider, "w 100lp");
		
		return panel;
	}
	
	
	public static class Config extends CloneableConfiguration {
		public double length;
	}
}
