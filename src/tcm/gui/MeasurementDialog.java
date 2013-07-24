package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import tcm.calibration.Calibration;
import tcm.configuration.Configuration;
import tcm.data.DataPoint;
import tcm.data.MeasurementInstance;
import tcm.data.MeasurementListener;
import tcm.data.MeasurementSource;
import tcm.defaults.Defaults;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;
import tcm.properties.PropertyList;
import tcm.properties.PropertyValue;
import tcm.properties.types.DoubleProperty;
import tcm.properties.types.IntegerProperty;
import tcm.properties.types.StringProperty;

import com.google.inject.Inject;
import com.google.inject.Provider;


public class MeasurementDialog extends JDialog {
	
	private static final String RECORD_TEXT = "Record";
	private static final String RECORDING_TEXT = "Recording...";
	
	private MeasurementInstance measurementInstance;
	
	private JToggleButton record;
	private JTextField backupDirField;
	private RealtimeGraph graph;
	private Calibration calibration = new Calibration();
	private PropertyList properties;
	
	private Recorder recorder;
	private BackupRecorder backupRecorder;
	
	private Provider<EditorFrame> editorProvider;
	
	
	@Inject
	public MeasurementDialog(RealtimeGraph graph, Provider<EditorFrame> editorProvider, Defaults defaults) {
		super(null, "Measuring...", ModalityType.MODELESS);
		
		this.editorProvider = editorProvider;
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		
		panel.add(new JLabel("Measurement properties:"), "wrap rel");
		
		properties = new PropertyList();
		properties.insert("Manufacturer", new PropertyValue(new StringProperty(), "Apogee"));
		properties.insert("Location", new PropertyValue(new StringProperty(), "Aapon mökki"));
		properties.insert("Test number", new PropertyValue(new IntegerProperty(), 3));
		properties.insert("Test decimal", new PropertyValue(new DoubleProperty(), 3.0));
		PropertyTable table = new PropertyTable(properties, true);
		panel.add(new JScrollPane(table), "grow, wrap para ");
		
		
		JButton button = new JButton("Calibrate");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CalibrationDialog(calibration, measurementInstance, MeasurementDialog.this).setVisible(true);
				MeasurementDialog.this.graph.setRange(measurementInstance.getMinimunValue(), measurementInstance.getMaximumValue(), calibration);
			}
		});
		panel.add(button, "growx, wrap para");
		
		
		record = new JToggleButton(RECORD_TEXT);
		record.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (record.isSelected()) {
					startRecord();
				} else {
					stopRecord();
				}
			}
		});
		panel.add(record, "growx, wrap para");
		
		
		JLabel label;
		String tip = "A backup of the recorded data is automatically stored in CSV format in the backup directory.  Leave empty for no backup.";
		label = new JLabel("Backup directory:");
		label.setToolTipText(tip);
		panel.add(label, "spanx, split");
		
		backupDirField = new JTextField();
		backupDirField.setText(defaults.getString("Measurement.backupDir", System.getProperty("java.io.tmpdir", "/tmp")));
		backupDirField.setEditable(true);
		backupDirField.setToolTipText(tip);
		panel.add(backupDirField, "growx, wrap para");
		
		
		split.setLeftComponent(panel);
		
		
		panel = new JPanel(new MigLayout("fill"));
		this.graph = graph;
		panel.add(graph, "grow, wrap");
		
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MeasurementDialog.this.dispose();
			}
		});
		panel.add(button, "right");
		
		split.setRightComponent(panel);
		
		
		this.add(split);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				stop();
			}
		});
		GUIUtil.setDisposableDialogOptions(this, null);
		split.setDividerLocation(0.3);
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
	
	public void stop() {
		if (measurementInstance == null)
			return;
		
		stopRecord();
		
		measurementInstance.removeListener(graph);
		measurementInstance.stop();
		measurementInstance = null;
	}
	
	
	private void startRecord() {
		if (recorder != null) {
			stopRecord();
		}
		
		
		recorder = new Recorder();
		measurementInstance.addListener(recorder);
		
		backupRecorder = new BackupRecorder();
		measurementInstance.addListener(backupRecorder);
		
		record.setText(RECORDING_TEXT);
	}
	
	private void stopRecord() {
		if (recorder == null) {
			return;
		}
		
		measurementInstance.removeListener(recorder);
		measurementInstance.removeListener(backupRecorder);
		backupRecorder.close();
		record.setText(RECORD_TEXT);
		
		Measurement m = new Measurement();
		m.getDataPoints().addAll(recorder.dataPoints);
		m.setCalibration(calibration.copy());
		m.setPropertyList(properties.copy());
		
		MeasurementDocument doc = new MeasurementDocument();
		doc.setMeasurement(m);
		
		EditorFrame frame = editorProvider.get();
		frame.setDocument(doc);
		frame.setVisible(true);
		
		recorder = null;
		backupRecorder = null;
	}
	
	
	private class Recorder implements MeasurementListener {
		
		private List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		private int timingMisses = 0;
		private int dataErrors = 0;
		
		private int lastReported = -1;
		
		@Override
		public void processData(List<DataPoint> data) {
			dataPoints.addAll(data);
			
			double start = dataPoints.get(0).getTime();
			double end = dataPoints.get(dataPoints.size() - 1).getTime();
			int duration = (int) (end - start);
			if (duration != lastReported) {
				record.setText(RECORDING_TEXT + " (" + duration + " s)");
			}
		}
		
		@Override
		public void timingMiss() {
			timingMisses++;
		}
		
		@Override
		public void dataError() {
			dataErrors++;
		}
		
	}
	
	
	private class BackupRecorder implements MeasurementListener {
		
		private BufferedWriter writer = null;
		
		public BackupRecorder() {
			File dir = new File(backupDirField.getText());
			if (!dir.isDirectory()) {
				System.err.println("WARNING:  '" + dir + "' is not a directory, not making backup recordings.");
				return;
			}
			
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String date = fmt.format(new Date());
			try {
				File file = File.createTempFile("ThrustCurveMeasurement-" + date + "-", ".csv", dir);
				System.out.println("Writing backup of measurement to " + file);
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("# Backup of measurement started on " + date + "\n");
			} catch (IOException e) {
				System.err.println("WARNING:  Unable to open backup file writer, not making backup: " + e.getMessage());
			}
		}
		
		public void close() {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
			writer = null;
		}
		
		@Override
		public void processData(List<DataPoint> data) {
			if (writer == null)
				return;
			
			for (DataPoint p : data) {
				write(p.getTime() + "," + p.getTimestamp() + "," + p.getValue() + "\n");
			}
		}
		
		@Override
		public void timingMiss() {
			write("# Timing miss\n");
		}
		
		@Override
		public void dataError() {
			write("# Data error\n");
		}
		
		private void write(String s) {
			if (writer != null) {
				try {
					writer.write(s);
				} catch (IOException e) {
					System.err.println("WARNING:  Error writing to backup file: " + e.getMessage());
					close();
				}
			}
		}
	}
	
}
