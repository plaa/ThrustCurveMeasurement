package tcm.arduinoad;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.util.Chars;
import tcm.configuration.Configuration;
import tcm.configuration.Configurator;

import com.google.inject.Inject;

public class ArduinoADConfigurator implements Configurator {
	
	private static final Integer[] SERIAL_SPEEDS = { 9600, 38400, 57600, 115200, 230400, 460800 };
	
	@Inject
	SerialDataCommunicator communicator;
	
	private boolean okPressed;
	
	private JComboBox serialDevice;
	private JComboBox serialSpeed;
	
	@Override
	public Configuration configure(Configuration current) {
		ArduinoADConfiguration config;
		if (current == null) {
			config = new ArduinoADConfiguration();
		} else {
			config = (ArduinoADConfiguration) current.copy();
		}
		
		JDialog dialog = buildDialog(config);
		dialog.setVisible(true);
		
		if (okPressed) {
			config.setSerialDevice((String) serialDevice.getSelectedItem());
			Object speed = serialSpeed.getSelectedItem();
			if (speed instanceof Number) {
				config.setSerialSpeed(((Number) speed).intValue());
			} else if ((speed instanceof String) && (((String) speed).matches("^[0-9]+$"))) {
				config.setSerialSpeed(Integer.parseInt((String) speed));
			}
			return config;
		} else {
			return null;
		}
	}
	
	private JDialog buildDialog(ArduinoADConfiguration config) {
		final JDialog dialog = new JDialog(null, "ArduinoAD configuration", ModalityType.APPLICATION_MODAL);
		okPressed = false;
		
		IntegerModel intModel;
		BooleanModel booleanModel;
		JSpinner spin;
		JLabel label;
		String tip;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		DescriptionArea desc = new DescriptionArea("This measurement source reads data from an Arduino functioning as an A/D converter. " +
				"The Arduino must be loaded with the ArduinoAD software.", 4, 0);
		panel.add(desc, "spanx, grow, wrap para");
		
		
		String[] ports = communicator.getSerialPorts();
		serialDevice = new JComboBox(ports);
		if (config.getSerialDevice().length() > 0) {
			serialDevice.setSelectedItem(config.getSerialDevice());
		} else if (ports.length > 0) {
			serialDevice.setSelectedItem(ports[0]);
		}
		serialDevice.setEditable(true);
		panel.add(new JLabel("Serial device:"));
		panel.add(serialDevice, "spanx, growx, wrap");
		
		
		serialSpeed = new JComboBox(SERIAL_SPEEDS);
		serialSpeed.setSelectedItem(config.getSerialSpeed());
		serialSpeed.setEditable(true);
		panel.add(new JLabel("Serial speed:"));
		panel.add(serialSpeed, "spanx, growx, wrap para");
		
		
		intModel = new IntegerModel(config, "Delay", 1);
		tip = "Delay between samples in microseconds.  The inverse of the sampling frequency.";
		label = new JLabel("Sampling delay:");
		label.setToolTipText(tip);
		panel.add(label);
		spin = new JSpinner(intModel.getSpinnerModel());
		spin.setToolTipText(tip);
		panel.add(spin, "growx, w 100lp");
		panel.add(new JLabel(Chars.MICRO + "s"), "wrap");
		
		
		intModel = new IntegerModel(config, "Input", 0, 5);
		panel.add(new JLabel("Analog input number:"));
		spin = new JSpinner(intModel.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		
		booleanModel = new BooleanModel(config, "ExternalReference");
		JCheckBox checkbox = new JCheckBox(booleanModel);
		checkbox.setText("Use external reference");
		checkbox.setToolTipText("Use Arduino external voltage reference instead of +5V reference");
		panel.add(checkbox, "skip 1, spanx, wrap para");
		
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				dialog.setVisible(false);
			}
		});
		panel.add(ok, "spanx, split, sg btn");
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		panel.add(cancel, "sg btn");
		
		dialog.add(panel);
		GUIUtil.setDisposableDialogOptions(dialog, ok);
		
		return dialog;
	}
	//	@Override
	//	public Configuration configure(Configuration current) {
	//		
	//		
	//		ArduinoADConfiguration config = new ArduinoADConfiguration("/dev/ttyACM3", 9600, 5000, false, false,
	//				new int[] { 0 }, new String[] { "Input1" });
	//		
	//		return config;
	//	}
	
}
