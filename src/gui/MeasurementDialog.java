package gui;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;

import com.google.inject.Inject;

import configuration.Configuration;
import data.DataPoint;
import data.MeasurementInstance;
import data.MeasurementListener;
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
		
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	
	public void start(MeasurementSource source, Configuration config) {
		
		measurementInstance = source.getInstance(config);
		measurementInstance.addListener(graph);
		graph.setRange(measurementInstance.getMinimunValue(), measurementInstance.getMaximumValue());
		try {
			measurementInstance.start();
		} catch (IOException e) {
			CharArrayWriter caw = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(caw));
			JOptionPane.showMessageDialog(null, caw.toString(), "Error starting measurement", JOptionPane.ERROR_MESSAGE);
		}
		
		this.setVisible(true);
	}
	
	private class DataListener implements MeasurementListener {
		
		@Override
		public void processData(List<DataPoint> data) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void timingMiss() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void dataError() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
