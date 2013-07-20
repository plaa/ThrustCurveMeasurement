package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import tcm.calibration.Calibration;
import tcm.configuration.Configuration;
import tcm.data.DataPoint;
import tcm.data.MeasurementInstance;
import tcm.data.MeasurementListener;
import tcm.data.MeasurementSource;
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
	private RealtimeGraph graph;
	private Calibration calibration = new Calibration();
	private PropertyList properties;
	
	private Recorder recorder;
	
	private Provider<EditorFrame> editorProvider;
	
	@Inject
	public MeasurementDialog(RealtimeGraph graph, Provider<EditorFrame> editorProvider) {
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
		record.setText(RECORDING_TEXT);
	}
	
	private void stopRecord() {
		if (recorder == null) {
			return;
		}
		
		measurementInstance.removeListener(recorder);
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
	
}
