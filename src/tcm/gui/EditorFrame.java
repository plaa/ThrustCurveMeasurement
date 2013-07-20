package tcm.gui;

import java.util.EventObject;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.StateChangeListener;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;
import tcm.filter.DataFilter;

import com.google.inject.Inject;

public class EditorFrame extends JFrame implements StateChangeListener {
	
	private MeasurementDocument document = new MeasurementDocument();
	
	private MeasurementGraph graph;
	private FilterPanel filterPanel;
	
	private boolean dirty = true;
	
	@Inject
	public EditorFrame(FilterPanel filterPanel) {
		super("Measurement editor");
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		
		
		this.filterPanel = filterPanel;
		split.setLeftComponent(filterPanel);
		filterPanel.setDocument(document);
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		graph = new MeasurementGraph();
		panel.add(graph, "grow");
		
		split.setRightComponent(panel);
		
		this.add(split);
		this.pack();
		this.setLocationByPlatform(true);
		split.setDividerLocation(0.3);
	}
	
	public void setDocument(MeasurementDocument document) {
		this.document.removeChangeListener(this);
		this.document = document;
		this.document.addChangeListener(this);
		filterPanel.setDocument(document);
		update();
	}
	
	public void update() {
		System.out.println("Running filtering and updating graph");
		Measurement original = document.getMeasurement().copy();
		Measurement filtered = document.getMeasurement().copy();
		
		for (DataFilter filter : document.getFilters()) {
			original = filter.filterOriginalData(original);
			filtered = filter.filter(filtered);
		}
		
		graph.update(original, filtered);
		
		dirty = false;
	}
	
	@Override
	public void stateChanged(EventObject e) {
		dirty = true;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (dirty) {
					update();
				}
			}
		});
	}
}
