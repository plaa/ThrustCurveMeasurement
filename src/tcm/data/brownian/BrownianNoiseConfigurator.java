package tcm.data.brownian;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.unit.UnitGroup;
import tcm.configuration.Configuration;
import tcm.configuration.Configurator;

public class BrownianNoiseConfigurator implements Configurator {
	
	private boolean okPressed;
	
	@Override
	public Configuration configure(Configuration current) {
		BrownianNoiseConfiguration config;
		if (current == null) {
			config = new BrownianNoiseConfiguration();
		} else {
			config = (BrownianNoiseConfiguration) current.copy();
		}
		
		JDialog dialog = buildDialog(config);
		dialog.setVisible(true);
		
		if (okPressed) {
			return config;
		} else {
			return null;
		}
	}
	
	
	private JDialog buildDialog(BrownianNoiseConfiguration config) {
		final JDialog dialog = new JDialog(null, "Brownian noise configuration", ModalityType.APPLICATION_MODAL);
		okPressed = false;
		
		DoubleModel model;
		IntegerModel intModel;
		JSpinner spin;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		DescriptionArea desc = new DescriptionArea("This measurement source generates Brownian noise. " +
				"It is intended for testing purposes.", 4, 0);
		panel.add(desc, "spanx, grow, wrap para");
		
		model = new DoubleModel(config, "Frequency", 1);
		panel.add(new JLabel("Frequency:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		panel.add(new JLabel("Hz"), "wrap");
		
		intModel = new IntegerModel(config, "Grouping", 1);
		panel.add(new JLabel("Data points per call:"));
		spin = new JSpinner(intModel.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		model = new DoubleModel(config, "Amplitude", 0);
		panel.add(new JLabel("Amplitude:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		model = new DoubleModel(config, "Minimum");
		panel.add(new JLabel("Minimum:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		model = new DoubleModel(config, "Maximum");
		panel.add(new JLabel("Maximum:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		model = new DoubleModel(config, "TimingMissProbability", UnitGroup.UNITS_RELATIVE, 0, 1);
		panel.add(new JLabel("Timing miss probability:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		panel.add(new UnitSelector(model), "wrap");
		
		model = new DoubleModel(config, "DataErrorProbability", UnitGroup.UNITS_RELATIVE, 0, 1);
		panel.add(new JLabel("Data error probability:"));
		spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		panel.add(new UnitSelector(model), "wrap");
		
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
	
}
