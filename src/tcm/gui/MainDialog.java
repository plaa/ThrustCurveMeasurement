package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.util.Named;
import tcm.configuration.Configuration;
import tcm.configuration.Configurator;
import tcm.data.MeasurementSource;

import com.google.inject.Inject;
import com.google.inject.Provider;


public class MainDialog extends JFrame {
	
	@Inject
	private Provider<MeasurementDialog> measurementDialog;
	
	@Inject
	private Provider<CSVOpenDialog> csvOpenDialog;
	
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
		panel.add(sourceSelector, "growx");
		
		JButton record = new JButton("Start measurement");
		record.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MeasurementSource source = ((Named<MeasurementSource>) sourceSelector.getSelectedItem()).get();
				startRecord(source);
			}
		});
		panel.add(record, "growx, wrap para");
		
		
		JButton open = new JButton("Open file");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		panel.add(open, "spanx, growx, wrap para");
		
		
		this.add(panel);
		
		this.pack();
		this.validate();
		this.setLocationByPlatform(true);
		
	}
	
	private void openFile() {
		System.out.println("here");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new SimpleFileFilter("Supported files", "csv"));
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			openFile(file);
		}
		
	}
	
	private void openFile(File file) {
		String[] name = file.getName().split("\\.");
		String extension = name[name.length - 1];
		
		if (extension.toLowerCase().equals("csv")) {
			csvOpenDialog.get().open(file);
		} else {
			JOptionPane.showMessageDialog(this, "Unknown file extension: " + file.getName(), "Unknown file", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	private void startRecord(MeasurementSource source) {
		Configurator configurator = source.getConfigurator();
		Configuration config = configurator.configure(null);
		if (config == null) {
			return;
		}
		measurementDialog.get().start(source, config);
	}
	
}
