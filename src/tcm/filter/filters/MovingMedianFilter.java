package tcm.filter.filters;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;

/*
 * Info at:
 *   http://www.analog.com/static/imported-files/tech_docs/dsp_book_Ch15.pdf‎
 *   https://en.wikipedia.org/wiki/Moving_average
 */
@Plugin
public class MovingMedianFilter extends AbstractDataFilter {
	
	@Override
	public String getName() {
		return "Moving median filter";
	}
	
	@Override
	public String getDescription() {
		return "Apply a moving median filter to the data. " +
				"A moving average filter is optimal if the noise is normally distributed, while a " +
				"moving median filter may be better if the signal has a disproportionate amount of " +
				"large errors.";
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		double length = getLength();
		List<Double> medianPoints = new ArrayList<Double>();
		
		int last = 0;
		int next = 0;
		double delta = length / 2;
		
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
		
		DoubleModel model = new DoubleModel(this, "Length", UnitGroup.UNITS_TIME_STEP, 0);
		panel.add(new JLabel("Length:"));
		
		JSpinner spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 60lp");
		
		UnitSelector unit = new UnitSelector(model);
		panel.add(unit);
		
		BasicSlider slider = new BasicSlider(model.getSliderModel(0.0, 0.25));
		panel.add(slider, "w 100lp");
		
		return panel;
	}
	
	
	public double getLength() {
		return configuration.getDouble("length", 0.1);
	}
	
	public void setLength(double length) {
		configuration.getMap().put("length", length);
		fireChangeEvent();
	}
	
}
