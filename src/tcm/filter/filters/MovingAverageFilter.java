package tcm.filter.filters;

import java.awt.Component;
import java.util.EventObject;
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
import net.sf.openrocket.util.StateChangeListener;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;

@Plugin
public class MovingAverageFilter extends AbstractDataFilter {
	
	private DoubleModel kernelLength = new DoubleModel(0.1, UnitGroup.UNITS_SHORT_TIME, 0);
	
	public MovingAverageFilter() {
		kernelLength.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				fireChangeEvent();
			}
		});
	}
	
	@Override
	public String getName() {
		return "Moving average filter";
	}
	
	@Override
	public String getDescription() {
		return "Apply a square moving average filter to the data.";
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		double sum = 0;
		int count = 0;
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
				sum += points.get(next).getValue();
				count++;
				next++;
			}
			while (last < n && points.get(last).getTime() < tMin) {
				sum -= points.get(last).getValue();
				count--;
				last++;
			}
			// Filter out very small negative values (rounding errors)
			if (sum < 0 && sum > -1e-9) {
				sum = 0;
			}
			result[i] = sum / count;
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
		
		BasicSlider slider = new BasicSlider(kernelLength.getSliderModel(0.0, 1.0));
		panel.add(slider, "w 100lp");
		
		return panel;
	}
}
