package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.unit.FixedPrecisionUnit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import tcm.data.DataPoint;
import tcm.document.Measurement;
import tcm.document.MeasurementDocument;
import tcm.file.CSVReader;
import tcm.properties.PropertyValue;
import tcm.properties.ProperyNames;
import tcm.properties.types.StringProperty;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CSVOpenDialog extends JDialog {
	
	private static final UnitGroup TIME_STEP;
	static {
		TIME_STEP = new UnitGroup();
		TIME_STEP.addUnit(new FixedPrecisionUnit("ns", 1, 0.000000001));
		TIME_STEP.addUnit(new FixedPrecisionUnit(Chars.MICRO + "s", 1, 0.000001));
		TIME_STEP.addUnit(new FixedPrecisionUnit("ms", 1, 0.001));
		TIME_STEP.addUnit(new FixedPrecisionUnit("s", 0.01));
		TIME_STEP.setDefaultUnit(3);
	}
	
	@Inject
	private Provider<EditorFrame> editorFrame;
	
	private JTextArea comments;
	private JTable data;
	
	private JSpinner timeColumn;
	private DoubleModel timeUnit;
	private JCheckBox timeReset;
	private JSpinner dataColumn;
	
	private boolean doLoad = false;
	
	public CSVOpenDialog() {
		super(null, "Import CSV", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		
		panel.add(new JLabel("Time column:"));
		timeColumn = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		panel.add(timeColumn);
		
		panel.add(new JLabel(" in units of "));
		timeUnit = new DoubleModel(1, TIME_STEP, 0);
		JSpinner spin = new JSpinner(timeUnit.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 100lp");
		panel.add(new UnitSelector(timeUnit), "wrap rel");
		
		timeReset = new JCheckBox("Start at zero");
		timeReset.setSelected(true);
		panel.add(timeReset, "skip 2, spanx, wrap para");
		
		
		panel.add(new JLabel("Data column:"));
		dataColumn = new JSpinner(new SpinnerNumberModel(2, 1, 10000, 1));
		panel.add(dataColumn, "wrap para");
		
		
		panel.add(new JLabel("Preview:"), "wrap rel");
		
		
		comments = new JTextArea(10, 40);
		comments.setEditable(false);
		panel.add(new JScrollPane(comments), "spanx, growx, wrap rel");
		
		data = new JTable();
		panel.add(new JScrollPane(data), "spanx, grow, wrap para");
		
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLoad = true;
				setVisible(false);
			}
		});
		panel.add(ok, "spanx, split, right");
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		panel.add(cancel, "right");
		
		this.add(panel);
		doLoad = false;
		GUIUtil.setDisposableDialogOptions(this, ok);
	}
	
	
	public void open(File file) {
		try {
			CSVReader reader = new CSVReader();
			Preview preview = new Preview();
			reader.readCSV(file, preview);
			
			String[] columnNames = new String[preview.columns];
			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = "Column " + (i + 1);
			}
			
			comments.setText(preview.comment.toString());
			comments.setCaretPosition(0);
			
			DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			String[] row = new String[preview.columns];
			for (List<String> line : preview.data) {
				model.addRow(line.toArray(row));
			}
			data.setModel(model);
			
			this.setVisible(true);
			
			if (!doLoad) {
				return;
			}
			
			Loader loader = new Loader(((Integer) timeColumn.getValue()) - 1, ((Integer) dataColumn.getValue()) - 1, timeUnit.getValue(),
					timeReset.isSelected());
			reader.readCSV(file, loader);
			
			Measurement measurement = new Measurement();
			measurement.getPropertyList().insert(ProperyNames.COMMENT, new PropertyValue(new StringProperty(), loader.comment.toString()));
			measurement.getDataPoints().addAll(loader.data);
			
			MeasurementDocument doc = new MeasurementDocument();
			doc.setMeasurement(measurement);
			
			EditorFrame frame = editorFrame.get();
			frame.setDocument(doc);
			frame.setVisible(true);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error reading " + file.getName() + ": " + e.getMessage(), "Error reading file",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	private class Preview implements CSVReader.CSVOutput {
		private int columns = 0;
		private List<List<String>> data = new ArrayList<List<String>>();
		private StringBuilder comment = new StringBuilder();
		
		private int count = 0;
		
		@Override
		public boolean line(List<String> values) {
			columns = Math.max(columns, values.size());
			data.add(values);
			
			count++;
			return count < 100;
		}
		
		@Override
		public boolean comment(String cmt) {
			if (columns == 0) {
				comment.append(cmt).append('\n');
			}
			
			count++;
			return count < 100;
		}
	}
	
	private class Loader implements CSVReader.CSVOutput {
		private List<DataPoint> data = new ArrayList<DataPoint>();
		private StringBuilder comment = new StringBuilder();
		
		private final int timeColumn;
		private final int dataColumn;
		private final double timeScale;
		
		private double timeDelta = Double.NaN;
		
		public Loader(int timeColumn, int dataColumn, double timeScale, boolean resetToZero) {
			this.timeColumn = timeColumn;
			this.dataColumn = dataColumn;
			this.timeScale = timeScale;
			if (resetToZero) {
				timeDelta = Double.NaN;
			} else {
				timeDelta = 0;
			}
		}
		
		@Override
		public boolean line(List<String> values) {
			try {
				double time = Double.NaN;
				double value = Double.NaN;
				
				if (values.size() > timeColumn) {
					time = Double.parseDouble(values.get(timeColumn).trim());
				}
				if (values.size() > dataColumn) {
					value = Double.parseDouble(values.get(dataColumn).trim());
				}
				
				if (!Double.isNaN(time) && !Double.isNaN(value)) {
					if (Double.isNaN(timeDelta)) {
						timeDelta = time * timeScale;
					}
					
					data.add(new DataPoint(time * timeScale - timeDelta, value, 0));
				}
				
			} catch (NumberFormatException e) {
				// Ignore
			}
			return true;
		}
		
		@Override
		public boolean comment(String cmt) {
			if (data.size() == 0) {
				comment.append(cmt).append('\n');
			}
			return true;
		}
		
	}
}
