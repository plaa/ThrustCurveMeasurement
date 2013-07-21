package tcm.filter.filters;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;
import tcm.data.DataPoint;
import tcm.defaults.Defaults;
import tcm.document.Measurement;
import tcm.filter.AbstractDataFilter;
import tcm.gui.adaptors.DoubleValue;

import com.google.inject.Inject;

@Plugin
public class AutoCropFilter extends AbstractDataFilter {
	
	private DoubleValue sampleTime = new DoubleValue(0.5);
	private DoubleValue additionalAllowance = new DoubleValue(1);
	private DoubleValue leaveTime = new DoubleValue(1);
	
	private StateChangeListener listener = new StateChangeListener() {
		@Override
		public void stateChanged(EventObject e) {
			fireChangeEvent();
		}
	};
	
	@Inject
	public AutoCropFilter(Defaults defaults) {
		sampleTime.addChangeListener(listener);
		additionalAllowance.addChangeListener(listener);
		leaveTime.addChangeListener(listener);
		defaults.remember(sampleTime, "AutoCropFilter.sample_time");
		defaults.remember(additionalAllowance, "AutoCropFilter.additional_allowance");
		defaults.remember(leaveTime, "AutoCropFilter.leave_time");
	}
	
	
	@Override
	public String getName() {
		return "Auto-crop";
	}
	
	@Override
	public String getDescription() {
		return "Automatically crop the start and end portions that lie within a similar range.";
	}
	
	@Override
	public Measurement filter(Measurement measurement) {
		List<DataPoint> points = measurement.getDataPoints();
		
		// Auto-crop start
		double startMin = Double.POSITIVE_INFINITY;
		double startMax = Double.NEGATIVE_INFINITY;
		double startMeasurement = points.get(0).getTime() + sampleTime.getValue();
		double startTimeLimit;
		
		ListIterator<DataPoint> iterator = points.listIterator();
		
		boolean first = true;
		while (true) {
			if (!iterator.hasNext()) {
				System.err.println("Autocrop would delete everything, ignoring.");
				return measurement;
			}
			
			DataPoint p = iterator.next();
			
			if (p.getTime() <= startMeasurement) {
				startMin = MathUtil.min(startMin, p.getValue());
				startMax = MathUtil.max(startMax, p.getValue());
			} else {
				if (first) {
					System.out.println("Initial startMin=" + startMin + " startMax=" + startMax);
					startMin = startMin - (startMax - startMin) * additionalAllowance.getValue();
					startMax = startMax + (startMax - startMin) * additionalAllowance.getValue();
					System.out.println("Final startMin=" + startMin + " startMax=" + startMax);
					first = false;
				}
				
				if (p.getValue() < startMin || p.getValue() > startMax) {
					System.out.println("Value out of bounds at " + p);
					startTimeLimit = p.getTime() - leaveTime.getValue();
					System.out.println("Crop before " + startTimeLimit);
					break;
				}
			}
		}
		
		while (iterator.hasPrevious()) {
			DataPoint p = iterator.previous();
			if (p.getTime() < startTimeLimit) {
				iterator.remove();
			}
		}
		
		
		
		
		// Auto-crop end
		double endMin = Double.POSITIVE_INFINITY;
		double endMax = Double.NEGATIVE_INFINITY;
		double endMeasurement = points.get(points.size() - 1).getTime() - sampleTime.getValue();
		double endTimeLimit;
		
		iterator = points.listIterator(points.size());
		
		first = true;
		while (true) {
			if (!iterator.hasPrevious()) {
				System.err.println("Autocrop from end would delete everything, ignoring.");
				return measurement;
			}
			
			DataPoint p = iterator.previous();
			
			if (p.getTime() >= endMeasurement) {
				endMin = MathUtil.min(endMin, p.getValue());
				endMax = MathUtil.max(endMax, p.getValue());
			} else {
				if (first) {
					endMin = endMin - (endMax - endMin) * additionalAllowance.getValue();
					endMax = endMax + (endMax - endMin) * additionalAllowance.getValue();
					first = false;
				}
				
				if (p.getValue() < endMin || p.getValue() > endMax) {
					endTimeLimit = p.getTime() + leaveTime.getValue();
					break;
				}
			}
		}
		
		while (iterator.hasNext()) {
			DataPoint p = iterator.next();
			if (p.getTime() > endTimeLimit) {
				iterator.remove();
			}
		}
		
		return measurement;
	}
	
	
	@Override
	public Component getConfigurationComponent() {
		JPanel panel = new JPanel(new MigLayout());
		
		String tip;
		JLabel label;
		JSpinner spin;
		UnitSelector unit;
		DoubleModel model;
		
		
		tip = "Amount of time to sample for zero level from start and end of data.";
		label = new JLabel("Sample range:");
		label.setToolTipText(tip);
		panel.add(label);
		
		model = new DoubleModel(sampleTime, "Value", UnitGroup.UNITS_SHORT_TIME, 0);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "w 40lp");
		
		unit = new UnitSelector(model);
		unit.setToolTipText(tip);
		panel.add(unit, "wrap unrel");
		
		
		
		tip = "Amount of data to leave before and after detected data range.";
		label = new JLabel("Leave time:");
		label.setToolTipText(tip);
		panel.add(label);
		
		model = new DoubleModel(leaveTime, "Value", UnitGroup.UNITS_SHORT_TIME, 0);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "w 40lp");
		
		unit = new UnitSelector(model);
		unit.setToolTipText(tip);
		panel.add(unit, "wrap unrel");
		
		
		
		tip = "Amount that values can exceed sampled range before detecting data, percentage of sampled range.";
		label = new JLabel("Range allowance:");
		label.setToolTipText(tip);
		panel.add(label);
		
		model = new DoubleModel(additionalAllowance, "Value", UnitGroup.UNITS_RELATIVE, 0);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "w 40lp");
		
		unit = new UnitSelector(model);
		unit.setToolTipText(tip);
		panel.add(unit, "wrap unrel");
		
		
		JButton button = new JButton("Defaults");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sampleTime.setValue(0.5);
				leaveTime.setValue(1.0);
				additionalAllowance.setValue(1.0);
			}
		});
		panel.add(button);
		
		return panel;
	}
}
