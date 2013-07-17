package gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.unit.UnitGroup;
import calibration.Calibration;
import data.DataPoint;
import data.MeasurementInstance;
import data.MeasurementListener;

public class CalibrationDialog extends JDialog {
	
	private static final double CALIBRATION_TIME = 5;
	
	private CalibrationOngoingDialog calibrationOngoingDialog;
	
	public CalibrationDialog(final Calibration calibration, final MeasurementInstance measurementInstance, final Window parent) {
		super(parent, "Calibration", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill, gap para"));
		DoubleModel model;
		JSpinner spin;
		UnitSelector unit;
		JButton button;
		
		panel.add(new StyledLabel("Raw value", Style.BOLD), "skip 1");
		panel.add(new StyledLabel("Calibration value", Style.BOLD), "wrap para");
		
		
		panel.add(new JLabel("Input 1:"));
		
		model = new DoubleModel(calibration, "InputValue1");
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "split 2, wmin 70lp, growx");
		
		button = new JButton("Cal");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double value = getCalibrationValue(measurementInstance);
				if (!Double.isNaN(value)) {
					calibration.setInputValue1(value);
				}
			}
		});
		panel.add(button, "");
		
		model = new DoubleModel(calibration, "OutputValue1", UnitGroup.UNITS_FORCE);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "split 2, growx");
		unit = new UnitSelector(model);
		unit.setSelectedUnit(UnitGroup.UNITS_FORCE.findApproximate("kgf"));
		panel.add(unit, "wrap");
		
		
		panel.add(new JLabel("Input 2:"));
		
		model = new DoubleModel(calibration, "InputValue2");
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "split 2, wmin 70lp, growx");
		
		button = new JButton("Cal");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double value = getCalibrationValue(measurementInstance);
				if (!Double.isNaN(value)) {
					calibration.setInputValue2(value);
				}
			}
		});
		panel.add(button, "");
		
		model = new DoubleModel(calibration, "OutputValue2", UnitGroup.UNITS_FORCE);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "split 2, growx");
		unit = new UnitSelector(model);
		unit.setSelectedUnit(UnitGroup.UNITS_FORCE.findApproximate("kgf"));
		panel.add(unit, "wrap para");
		
		
		
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CalibrationDialog.this.setVisible(false);
			}
		});
		panel.add(button, "span, split, right");
		
		this.add(panel);
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	private double getCalibrationValue(MeasurementInstance measurementInstance) {
		CalibrationDataListener listener = new CalibrationDataListener();
		measurementInstance.addListener(listener);
		
		calibrationOngoingDialog = new CalibrationOngoingDialog();
		calibrationOngoingDialog.setVisible(true);
		
		measurementInstance.removeListener(listener);
		
		if (calibrationOngoingDialog.cancelled) {
			return Double.NaN;
		}
		
		double avg = 0;
		for (DataPoint dp : listener.data) {
			avg += dp.getValue();
		}
		avg /= listener.data.size();
		
		return avg;
	}
	
	private class CalibrationDataListener implements MeasurementListener {
		private final List<DataPoint> data = new ArrayList<DataPoint>();
		
		@Override
		public void processData(List<DataPoint> newData) {
			data.addAll(newData);
			if (data.get(data.size() - 1).getTime() - data.get(0).getTime() >= CALIBRATION_TIME + 1) {
				calibrationOngoingDialog.setVisible(false);
			}
		}
		
		@Override
		public void timingMiss() {
		}
		
		@Override
		public void dataError() {
		}
	}
	
	private class CalibrationOngoingDialog extends JDialog {
		private boolean cancelled = false;
		
		public CalibrationOngoingDialog() {
			super(CalibrationDialog.this, "Calibrating...", ModalityType.APPLICATION_MODAL);
			
			JPanel panel = new JPanel(new MigLayout("fill"));
			
			panel.add(new JLabel("Calibrating, please wait..."));
			JButton button = new JButton("Cancel");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					CalibrationOngoingDialog.this.setVisible(false);
				}
			});
			
			this.add(panel);
			
			this.setLocationByPlatform(true);
			this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			this.pack();
		}
	}
}
