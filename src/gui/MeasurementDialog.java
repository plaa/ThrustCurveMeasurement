package gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;

import com.google.inject.Inject;

import configuration.Configuration;
import data.MeasurementInstance;
import data.MeasurementSource;

public class MeasurementDialog extends JDialog {
	
	private MeasurementInstance measurementInstance;
	
	private RealtimeGraph graph;
	
	@Inject
	public MeasurementDialog(RealtimeGraph graph) {
		super(null, "Measuring...", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		this.graph = graph;
		panel.add(graph, "grow");
		
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
		graph.setRange(measurementInstance.getMinimunValue(), measurementInstance.getMaximumValue());
		
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
