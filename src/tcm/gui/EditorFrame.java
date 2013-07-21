package tcm.gui;

import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

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
	private JProgressBar filterProgressBar;
	
	private FilterWorker filterWorker;
	
	private boolean dirty = true;
	
	private long t0;
	
	@Inject
	public EditorFrame(FilterPanel filterPanel) {
		super("Measurement editor");
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		
		
		this.filterPanel = filterPanel;
		split.setLeftComponent(filterPanel);
		filterPanel.setDocument(document);
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		graph = new MeasurementGraph();
		panel.add(graph, "grow, wrap para");
		
		filterProgressBar = new JProgressBar();
		filterProgressBar.setString("");
		filterProgressBar.setStringPainted(true);
		panel.add(filterProgressBar, "span, growx");
		
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
		if (filterWorker != null) {
			filterWorker.cancel(true);
		}
		
		filterWorker = new FilterWorker(document.copy());
		filterWorker.execute();
		
		t0 = System.currentTimeMillis();
		System.out.println((System.currentTimeMillis() - t0) + ": Start background worker");
		filterProgressBar.setValue(0);
		filterProgressBar.setString("Filtering...");
		
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
	
	
	private class FilterResult {
		public Measurement original;
		public Measurement filtered;
	}
	
	private class FilterProgress {
		public int current;
		public int filterCount;
		public String currentFilter;
	}
	
	private class FilterWorker extends SwingWorker<FilterResult, FilterProgress> {
		private final MeasurementDocument doc;
		
		public FilterWorker(MeasurementDocument doc) {
			this.doc = doc;
		}
		
		@Override
		protected FilterResult doInBackground() throws Exception {
			System.out.println("Running filtering and updating graph");
			
			Measurement original = doc.getMeasurement().copy();
			Measurement filtered = doc.getMeasurement().copy();
			
			for (int i = 0; i < doc.getFilters().size(); i++) {
				DataFilter filter = doc.getFilters().get(i);
				
				FilterProgress progress = new FilterProgress();
				progress.current = i;
				progress.currentFilter = filter.getName();
				progress.filterCount = doc.getFilters().size();
				publish(progress);
				
				System.out.println((System.currentTimeMillis() - t0) + ": Publish filtering using " + progress.currentFilter);
				
				original = filter.filterOriginalData(original);
				if (isCancelled()) {
					return null;
				}
				filtered = filter.filter(filtered);
				if (isCancelled()) {
					return null;
				}
			}
			
			System.out.println((System.currentTimeMillis() - t0) + ": Publishing final result");
			FilterResult result = new FilterResult();
			result.original = original;
			result.filtered = filtered;
			
			return result;
		}
		
		@Override
		protected void process(List<FilterProgress> chunks) {
			FilterProgress progress = chunks.get(chunks.size() - 1);
			System.out.println((System.currentTimeMillis() - t0) + ": Received process of " + progress.currentFilter + " updating bar");
			int p = 100 * (progress.current + 1) / (progress.filterCount + 2);
			filterProgressBar.setValue(p);
			filterProgressBar.setString("Filtering (" + progress.currentFilter + ")...");
		}
		
		@Override
		protected void done() {
			if (isCancelled())
				return;
			
			FilterResult result;
			try {
				result = get();
				System.out.println((System.currentTimeMillis() - t0) + ": Got final result, starting graph update");
				graph.update(result.original, result.filtered);
				System.out.println((System.currentTimeMillis() - t0) + ": Graph update done");
				
				filterProgressBar.setValue(100);
				filterProgressBar.setString("Up-to-date");
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
		
	}
}
