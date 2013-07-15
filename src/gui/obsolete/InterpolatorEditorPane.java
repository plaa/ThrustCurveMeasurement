package gui.obsolete;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.TextUtil;
import util.Interpolator;

public class InterpolatorEditorPane extends JPanel {
	
	private final TextfieldListener fieldListener = new TextfieldListener();
	private final Interpolator interpolator;
	private final Calibrator calibrator;
	private boolean updating = false;
	
	private final List<Pair<JTextField, JTextField>> textFields =
			new ArrayList<Pair<JTextField, JTextField>>();
	private final List<JButton> calibrationButtons = new ArrayList<JButton>();
	private final List<JButton> deleteButtons = new ArrayList<JButton>();
	
	private final JButton addButton;
	private final JButton tareButton;
	private final JButton oneButton;
	private final JButton v5Button;
	private final JButton clearButton;
	
	public InterpolatorEditorPane(final Interpolator interpolator, final Calibrator calibrator) {
		super(new MigLayout("fillx"));
		
		this.interpolator = interpolator;
		this.calibrator = calibrator;
		
		addButton = new JButton("Add");
		addButton.setToolTipText("Add new calibration point");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField xField = new JTextField(TextUtil.doubleToString(
						calibrator.getCalibrationValue()));
				JTextField yField = new JTextField();
				
				int sel = JOptionPane.showConfirmDialog(InterpolatorEditorPane.this,
						new Object[] {
								"Add new calibration point:",
								xField,
								yField
						},
						"Add point", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (sel != JOptionPane.OK_OPTION)
					return;
				
				double x, y;
				try {
					x = Double.parseDouble(xField.getText());
					y = Double.parseDouble(yField.getText());
				} catch (NumberFormatException ex) {
					return;
				}
				
				updating = true;
				interpolator.addPoint(x, y);
				updating = false;
				updateFields();
			}
		});
		
		tareButton = new JButton("Tare");
		tareButton.setToolTipText("Subtract the current output value from all of the output calibration values, " +
				"current input (raw) value -> zero");
		tareButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double currentX = calibrator.getCalibrationValue();
				if (Double.isNaN(currentX))
					return;
				double currentY = interpolator.getValue(currentX);
				
				double[] x = interpolator.getXPoints();
				double[] y = interpolator.getYPoints();
				
				for (int i = 0; i < y.length; i++) {
					y[i] -= currentY;
				}
				
				updating = true;
				interpolator.clear();
				interpolator.addPoints(x, y);
				updating = false;
				
				updateFields();
			}
		});
		
		oneButton = new JButton("1:1");
		oneButton.setToolTipText("Set one-to-one conversion, output value = raw value");
		oneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updating = true;
				interpolator.clear();
				interpolator.addPoint(0, 0);
				interpolator.addPoint(1023, 1023);
				updating = false;
				updateFields();
			}
		});
		
		v5Button = new JButton("5V");
		v5Button.setToolTipText("Set conversion to display range 0 ... 5 V");
		v5Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updating = true;
				interpolator.clear();
				interpolator.addPoint(0, 0);
				interpolator.addPoint(1023, 5.0);
				updating = false;
				updateFields();
			}
		});
		
		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Remove all calibration points");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updating = true;
				interpolator.clear();
				updating = false;
				updateFields();
			}
		});
		
		updateFields();
	}
	
	
	private void updateFields() {
		updating = true;
		
		double[] xPoints = interpolator.getXPoints();
		double[] yPoints = interpolator.getYPoints();
		int count = xPoints.length;
		
		while (textFields.size() > count) {
			textFields.remove(textFields.size() - 1);
			calibrationButtons.remove(calibrationButtons.size() - 1);
		}
		while (textFields.size() < count) {
			JTextField field1 = new JTextField();
			JTextField field2 = new JTextField();
			field1.addActionListener(fieldListener);
			field2.addActionListener(fieldListener);
			textFields.add(new Pair<JTextField, JTextField>(field1, field2));
			
			JButton button = new JButton("Cal");
			button.setToolTipText("Set the current signal value to the input (raw) value of this field");
			button.addActionListener(new CalibrationListener(calibrationButtons.size()));
			calibrationButtons.add(button);
			
			button = new JButton("X");
			button.setToolTipText("Remove this calibration point");
			button.addActionListener(new DeleteListener(deleteButtons.size()));
			deleteButtons.add(button);
		}
		
		this.removeAll();
		for (int i = 0; i < count; i++) {
			JTextField fieldX = textFields.get(i).getU();
			JTextField fieldY = textFields.get(i).getV();
			fieldX.setToolTipText("Input (raw) value");
			fieldY.setToolTipText("Output value");
			fieldX.setText(TextUtil.doubleToString(xPoints[i]));
			fieldY.setText(TextUtil.doubleToString(yPoints[i]));
			this.add(calibrationButtons.get(i));
			this.add(fieldX, "sizegroup fields, growx");
			this.add(fieldY, "sizegroup fields, growx");
			this.add(deleteButtons.get(i), "wrap para");
		}
		
		this.add(addButton, "span, split");
		this.add(tareButton);
		this.add(v5Button);
		this.add(oneButton);
		this.add(clearButton);
		
		updating = false;
		this.revalidate();
		this.repaint();
	}
	
	
	private void readFields() {
		
		interpolator.clear();
		for (Pair<JTextField, JTextField> values : textFields) {
			String value1 = values.getU().getText();
			String value2 = values.getV().getText();
			double x = 0;
			double y = 0;
			
			try {
				x = Double.parseDouble(value1);
			} catch (NumberFormatException ignore) {
			}
			try {
				y = Double.parseDouble(value2);
			} catch (NumberFormatException ignore) {
			}
			
			interpolator.addPoint(x, y);
		}
		
		updateFields();
		
	}
	
	private class TextfieldListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!updating)
				readFields();
		}
	}
	
	private class CalibrationListener implements ActionListener {
		private final int point;
		
		public CalibrationListener(int n) {
			this.point = n;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Iterator<Map.Entry<Double, Double>> iterator = interpolator.iterator();
			Map.Entry<Double, Double> entry = null;
			int n = point;
			while (iterator.hasNext() && n >= 0) {
				entry = iterator.next();
				n--;
			}
			if (n >= 0 || entry == null)
				return;
			
			updating = true;
			
			double x = calibrator.getCalibrationValue();
			if (Double.isNaN(x))
				return;
			double y = entry.getValue();
			iterator.remove();
			interpolator.addPoint(x, y);
			
			updating = false;
			updateFields();
		}
	}
	
	


	private class DeleteListener implements ActionListener {
		private final int point;
		
		public DeleteListener(int n) {
			this.point = n;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			Iterator<Map.Entry<Double, Double>> iterator = interpolator.iterator();
			Map.Entry<Double, Double> entry = null;
			int n = point;
			while (iterator.hasNext() && n >= 0) {
				entry = iterator.next();
				n--;
			}
			if (n >= 0 || entry == null)
				return;
			
			updating = true;
			
			iterator.remove();
			
			updating = false;
			updateFields();
		}
	}
	
}
