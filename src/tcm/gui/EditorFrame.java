package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.util.StateChangeListener;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;
import tcm.file.FileSaver;
import tcm.file.XMLSaver;
import tcm.filter.DataFilter;

import com.google.inject.Inject;

public class EditorFrame extends JFrame implements StateChangeListener {
	
	private MeasurementDocument document = new MeasurementDocument();
	
	private MeasurementGraph graph;
	private FilterPanel filterPanel;
	private JProgressBar filterProgressBar;
	
	private FilterWorker filterWorker;
	
	private boolean dirty = true;
	
	@Inject
	private XMLSaver xmlSaver;
	
	@Inject
	private Set<FileSaver> fileSavers;
	
	
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
		panel.add(filterProgressBar, "span, growx, wrap para");
		
		
		JButton save = new JButton("Save / Export");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		panel.add(save);
		
		
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
		
		filterProgressBar.setValue(0);
		filterProgressBar.setString("Filtering...");
		
		dirty = false;
	}
	
	
	private void saveFile() {
		List<String> allExt = new ArrayList<String>();
		for (FileSaver l : fileSavers) {
			allExt.addAll(l.getExtensions());
		}
		
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new SimpleFileFilter("All supported files", allExt.toArray(new String[0])));
		for (FileSaver l : fileSavers) {
			chooser.addChoosableFileFilter(new SimpleFileFilter(l.getName(), l.getExtensions().toArray(new String[0])));
		}
		
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			saveFile(file);
		}
	}
	
	private void saveFile(File file) {
		String[] name = file.getName().split("\\.");
		String extension = name[name.length - 1];
		
		if (name.length == 1) {
			file = new File(file.getAbsolutePath() + ".tcm");
			extension = "tcm";
		}
		
		try {
			for (FileSaver l : fileSavers) {
				if (l.getExtensions().contains(extension)) {
					l.save(file, document);
					return;
				}
			}
			
			JOptionPane.showMessageDialog(this, "Unknown file extension: " + file.getName(), "Unknown file", JOptionPane.ERROR_MESSAGE);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, new Object[] {
					"Error saving file " + file.getName(),
					e.getMessage()
			}, "Saving error", JOptionPane.ERROR_MESSAGE);
		}
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
			Measurement original = doc.getMeasurement().copy();
			Measurement filtered = doc.getMeasurement().copy();
			
			for (int i = 0; i < doc.getFilters().size(); i++) {
				DataFilter filter = doc.getFilters().get(i);
				
				FilterProgress progress = new FilterProgress();
				progress.current = i;
				progress.currentFilter = filter.getName();
				progress.filterCount = doc.getFilters().size();
				publish(progress);
				
				original = filter.filterOriginalData(original);
				if (isCancelled()) {
					return null;
				}
				filtered = filter.filter(filtered);
				if (isCancelled()) {
					return null;
				}
			}
			
			FilterResult result = new FilterResult();
			result.original = original;
			result.filtered = filtered;
			
			return result;
		}
		
		@Override
		protected void process(List<FilterProgress> chunks) {
			FilterProgress progress = chunks.get(chunks.size() - 1);
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
				graph.update(result.original, result.filtered);
				
				filterProgressBar.setValue(100);
				filterProgressBar.setString("Up-to-date");
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
		
	}
}
