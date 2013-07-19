package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.Named;

import com.google.inject.Inject;

import configuration.Configuration;
import configuration.Configurator;
import data.MeasurementSource;

public class MainDialog extends JFrame {
	
	@Inject
	private MeasurementDialog measurementDialog;
	
	@SuppressWarnings("unchecked")
	@Inject
	public MainDialog(Set<MeasurementSource> measurementSources) {
		super("Thrust curve measurement");
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		Vector<Named<MeasurementSource>> values = new Vector<Named<MeasurementSource>>();
		for (MeasurementSource s : measurementSources) {
			values.add(new Named<MeasurementSource>(s, s.getName()));
		}
		Collections.sort(values);
		final JComboBox sourceSelector = new JComboBox(values);
		sourceSelector.setEditable(false);
		panel.add(sourceSelector);
		
		JButton record = new JButton("Start measurement");
		record.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MeasurementSource source = ((Named<MeasurementSource>) sourceSelector.getSelectedItem()).get();
				startRecord(source);
			}
		});
		panel.add(record);
		
		this.add(panel);
		
		this.pack();
		this.validate();
		this.setLocationByPlatform(true);
		
	}
	
	
	
	private void startRecord(MeasurementSource source) {
		Configurator configurator = source.getConfigurator();
		Configuration config = configurator.configure(null);
		if (config == null) {
			return;
		}
		measurementDialog.start(source, config);
	}
	
}
