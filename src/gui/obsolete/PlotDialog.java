package gui.obsolete;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.unit.UnitGroup;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import arduinoad.ArduinoConfiguration;
import arduinoad.DataListener;
import arduinoad.DataVO;
import arduinoad.SerialDataCommunicator;


public class PlotDialog extends JDialog implements DataListener {
	
	
	private static final long MAGIC_LONG = -666;
	
	private final XYSeries[] series;
	private final JCheckBox listeningBox;
	private final JCheckBox averageBox;
	private final DoubleModel sampleDifference;
	
	private long startTime = MAGIC_LONG;
	
	
	public PlotDialog(Window parent, ArduinoConfiguration config) {
		super(parent, "Data plot", ModalityType.MODELESS);
		
		XYSeriesCollection collection = new XYSeriesCollection();
		series = new XYSeries[SerialDataCommunicator.INPUTS];
		
		int[] input = config.getInputArray();
		String[] inputNames = config.getInputNamesArray();
		for (int i = 0; i < input.length; i++) {
			series[input[i]] = new XYSeries(inputNames[i]);
			collection.addSeries(series[input[i]]);
		}
		
		
		// Create the chart using the factory to get all default settings
		JFreeChart chart = ChartFactory.createXYLineChart(
				null,
				"Time / s",
				"Filtered value",
				collection,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true, // save
				false, // print
				true, // zoom
				true); // tooltips
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(chartPanel, "grow, span, wrap");
		
		listeningBox = new JCheckBox("Listening");
		listeningBox.setToolTipText("Uncheck box to stop listening to data for a while");
		listeningBox.setSelected(true);
		panel.add(listeningBox, "span, split");
		
		averageBox = new JCheckBox("Average");
		averageBox.setToolTipText("Whether to average the samples between plot points or select a single value");
		averageBox.setSelected(true);
		panel.add(averageBox, "gapleft para");
		
		
		// Sample distance
		panel.add(new JLabel("Sample distance:"), "gapleft para");
		sampleDifference = new DoubleModel(0.1, DataAnalyzer.UNITS_DELAY, 0);
		JSpinner spin = new JSpinner(sampleDifference.getSpinnerModel());
		spin.setToolTipText("Minimum amount of time between plotted samples");
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "gapleft rel");
		panel.add(new UnitSelector(sampleDifference), "");
		
		
		// Maximum sample count
		panel.add(new JLabel("Plot length:"), "gapleft para");
		final int readDelay = config.getDelay();
		final DoubleModel model = new DoubleModel(0, UnitGroup.UNITS_FLIGHT_TIME, 0);
		spin = new JSpinner(model.getSpinnerModel());
		spin.setToolTipText("Length of time to display in the plot");
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "gapleft rel");
		panel.add(new UnitSelector(model), "");
		
		
		ChangeListener listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double seconds = model.getValue();
				int diff = (int) Math.max(sampleDifference.getValue() * 1000000, readDelay);
				int samples = (int) (1000000 * seconds / diff);
				samples = Math.min(samples, 1000);
				System.out.println("Setting max item count to " + samples + " seconds=" + seconds + " diff=" + diff);
				for (XYSeries s : series) {
					if (s != null) {
						s.setMaximumItemCount(samples);
					}
				}
			}
		};
		model.addChangeListener(listener);
		sampleDifference.addChangeListener(listener);
		
		// By default keep 10 seconds of data
		model.setValue(10);
		
		
		panel.add(new JPanel(), "growx");
		
		
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (XYSeries s : series) {
					if (s != null) {
						s.clear();
					}
				}
				startTime = MAGIC_LONG;
			}
		});
		panel.add(clear, "right");
		
		
		this.add(panel);
		this.pack();
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	private long nextDataTimeStamp = Long.MIN_VALUE;
	
	@Override
	public void processData(List<DataVO> data) {
		boolean notify = listeningBox.isSelected();
		
		double[] average = new double[SerialDataCommunicator.INPUTS];
		int[] count = new int[SerialDataCommunicator.INPUTS];
		
		for (DataVO d : data) {
			if (d.isByteMissObject())
				continue;
			
			// Calculate average
			for (int i = 0; i < SerialDataCommunicator.INPUTS; i++) {
				if (d.getRawValue(i) < 0 || Double.isNaN(d.getFilteredValue(i)))
					continue;
				average[i] += d.getFilteredValue(i);
				count[i]++;
			}
			
			// Check if it's time to add a point
			if (d.getTimeStamp() < nextDataTimeStamp) {
				continue;
			}
			nextDataTimeStamp = d.getTimeStamp() + (long) (sampleDifference.getValue() * 1000000);
			
			// Check starting time is initialized
			if (startTime == MAGIC_LONG) {
				startTime = d.getTimeStamp();
			}
			
			// Add point
			double t = (d.getTimeStamp() - startTime) / 1000000.0;
			if (averageBox.isSelected()) {
				for (int i = 0; i < SerialDataCommunicator.INPUTS; i++) {
					if (series[i] == null || count[i] == 0)
						continue;
					average[i] /= count[i];
					series[i].add(t, average[i]);
				}
			} else {
				for (int i = 0; i < SerialDataCommunicator.INPUTS; i++) {
					if (d.getRawValue(i) < 0 || Double.isNaN(d.getFilteredValue(i)) || series[i] == null)
						continue;
					series[i].add(t, d.getFilteredValue(i), notify);
				}
			}
			
			Arrays.fill(average, 0);
			Arrays.fill(count, 0);
		}
		
		
	}
}
