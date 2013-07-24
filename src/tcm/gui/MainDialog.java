package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import tcm.defaults.Defaults;
import tcm.document.MeasurementDocument;
import tcm.file.FileLoader;

import com.google.inject.Inject;
import com.google.inject.Provider;


public class MainDialog extends JFrame {
	
	@Inject
	private Provider<MeasurementDialog> measurementDialog;
	
	@Inject
	private Provider<EditorFrame> editorFrame;
	
	@Inject
	private Set<FileLoader> fileLoaders;
	
	@Inject
	private Defaults defaults;
	
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
		
		
		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		panel.add(quit, "spanx, growx, wrap para");
		
		this.add(panel);
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		
		this.pack();
		this.validate();
		this.setLocationByPlatform(true);
		
	}
	
	private void openFile() {
		List<String> allExt = new ArrayList<String>();
		for (FileLoader l : fileLoaders) {
			allExt.addAll(l.getExtensions());
		}
		
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(defaults.getFile("previousFile", null));
		chooser.setFileFilter(new SimpleFileFilter("All supported files", allExt.toArray(new String[0])));
		for (FileLoader l : fileLoaders) {
			chooser.addChoosableFileFilter(new SimpleFileFilter(l.getName(), l.getExtensions().toArray(new String[0])));
		}
		
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			defaults.putFile("previousFile", file);
			openFile(file);
		}
	}
	
	private void openFile(File file) {
		String[] name = file.getName().split("\\.");
		String extension = name[name.length - 1];
		
		try {
			for (FileLoader l : fileLoaders) {
				if (l.getExtensions().contains(extension)) {
					MeasurementDocument doc;
					doc = l.load(file);
					
					if (doc != null) {
						EditorFrame frame = editorFrame.get();
						frame.setDocument(doc);
						frame.setFile(file);
						frame.setModified(false);
						frame.setVisible(true);
					}
					return;
				}
			}
			
			JOptionPane.showMessageDialog(this, "Unknown file extension: " + file.getName(), "Unknown file", JOptionPane.ERROR_MESSAGE);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, new Object[] {
					"Error loading file " + file.getName(),
					e.getMessage()
			}, "Loading error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void quit() {
		int value = JOptionPane.showConfirmDialog(MainDialog.this, "Are you sure you want to quit?", "Quit?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (value == JOptionPane.OK_OPTION) {
			System.exit(0);
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
