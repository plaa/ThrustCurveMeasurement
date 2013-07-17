package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import properties.PropertyList;
import properties.PropertyValue;
import properties.types.StringProperty;
import calibration.Calibration;

import com.google.inject.Inject;

import configuration.Configuration;
import data.MeasurementInstance;
import data.MeasurementSource;

public class MeasurementDialog extends JDialog {
	
	private MeasurementInstance measurementInstance;
	
	private RealtimeGraph graph;
	private Calibration calibration = new Calibration();
	
	@Inject
	public MeasurementDialog(RealtimeGraph graph) {
		super(null, "Measuring...", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		JPanel panel2 = new JPanel(new MigLayout("fill"));
		
		JButton button = new JButton("Calibrate");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CalibrationDialog(calibration, measurementInstance, MeasurementDialog.this).setVisible(true);
				System.out.println("Calibration: " + calibration);
				MeasurementDialog.this.graph.setRange(measurementInstance.getMinimunValue(), measurementInstance.getMaximumValue(), calibration);
			}
		});
		panel2.add(button, "wrap para");
		
		
		PropertyList properties = new PropertyList();
		properties.insert("manufacturer", "Manufacturer", new PropertyValue(new StringProperty(), "Apogee"));
		properties.insert("location", "Location", new PropertyValue(new StringProperty(), "Aapon mökki"));
		PropertyTable table = new PropertyTable(properties, false);
		panel2.add(new JScrollPane(table), "grow");
		
		panel.add(panel2, "gapright para, aligny 0");
		
		
		this.graph = graph;
		panel.add(graph, "grow, wrap para");
		
		
		
		this.add(panel);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				measurementInstance.stop();
				measurementInstance.removeListener(MeasurementDialog.this.graph);
			}
		});
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	public void start(MeasurementSource source, Configuration config) {
		
		measurementInstance = source.getInstance(config);
		measurementInstance.addListener(graph);
		graph.reset();
		graph.setRange(measurementInstance.getMinimunValue(), measurementInstance.getMaximumValue(), calibration);
		
		try {
			measurementInstance.start();
		} catch (IOException e) {
			CharArrayWriter caw = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(caw));
			JTextArea text = new JTextArea(caw.toString());
			text.setEditable(false);
			JOptionPane.showMessageDialog(null, new JScrollPane(text), "Error starting measurement", JOptionPane.ERROR_MESSAGE);
		}
		
		this.setVisible(true);
	}
	
	
}
