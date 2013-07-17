package gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import calibration.Calibration;

import com.google.inject.Inject;

import data.DataPoint;
import data.MeasurementListener;

public class RealtimeGraph extends JPanel implements MeasurementListener {
	
	private static final long TIME_DELTA = 100;
	private static final double PLOT_RANGE = 10;
	private static final double COMPUTE_RANGE = 10;
	
	private RealtimeGraphCreator graphCreator;
	private XYSeries[] series;
	private LinkedList<DataPoint> dataPoints = new LinkedList<DataPoint>();
	
	private List<DataPoint> pendingDataPoints = new ArrayList<DataPoint>();
	private long nextFlushTime = 0;
	
	private int dataPointCount = 0;
	private int dataErrorCount = 0;
	private int timingMissCount = 0;
	
	private JFreeChart chart;
	
	private JLabel dataCount;
	private JLabel dataRate;
	private JLabel actualRate;
	private JLabel timingMisses;
	private JLabel dataErrors;
	
	
	@Inject
	public RealtimeGraph(RealtimeGraphCreator graphCreator) {
		super(new MigLayout("fill"));
		
		this.graphCreator = graphCreator;
		
		XYSeriesCollection collection = new XYSeriesCollection();
		series = new XYSeries[graphCreator.getCount()];
		
		for (int i = 0; i < series.length; i++) {
			series[i] = new XYSeries(graphCreator.getName(i));
			collection.addSeries(series[i]);
		}
		
		
		// Create the chart using the factory to get all default settings
		chart = ChartFactory.createXYLineChart(
				null,
				"Time / s",
				"Value",
				collection,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		for (int i = 0; i < series.length; i++) {
			plot.getRenderer().setSeriesPaint(i, graphCreator.getPaint(i));
		}
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true, // save
				false, // print
				true, // zoom
				true); // tooltips
		
		this.add(chartPanel, "span, grow, wrap para");
		
		JLabel label;
		String tip;
		
		tip = "Number of data points measured so far";
		label = new JLabel("Data points:");
		label.setToolTipText(tip);
		this.add(label);
		dataCount = new JLabel();
		dataCount.setToolTipText(tip);
		this.add(dataCount, "gapright para");
		
		tip = "Data rate, as indicated by measurement source";
		label = new JLabel("Data rate:");
		label.setToolTipText(tip);
		this.add(label);
		dataRate = new JLabel();
		dataRate.setToolTipText(tip);
		this.add(dataRate, "gapright para");
		
		tip = "<html>Actual data rate, measured based on when data points are received.<br>" +
				"If this is lower than the data rate, it may indicate problems in data acquisition.";
		label = new JLabel("Actual rate:");
		label.setToolTipText(tip);
		this.add(label);
		actualRate = new JLabel();
		actualRate.setToolTipText(tip);
		this.add(actualRate, "gapright para");
		
		tip = "<html>Number of times measurement has been delayed later than intended, as indicated by measurement source.<br>" +
				"Continuous timing missings may indicate too high a sampling rate.";
		label = new JLabel("Timing misses:");
		label.setToolTipText(tip);
		this.add(label);
		timingMisses = new JLabel();
		timingMisses.setToolTipText(tip);
		this.add(timingMisses, "gapright para");
		
		tip = "Number of data errors that have been detected by the measurement source.";
		label = new JLabel("Data errors:");
		label.setToolTipText(tip);
		this.add(label);
		dataErrors = new JLabel();
		dataErrors.setToolTipText(tip);
		this.add(dataErrors, "gapright para");
	}
	
	public void setRange(double min, double max, Calibration cal) {
		XYPlot plot = (XYPlot) chart.getPlot();
		NumberAxis axis = new NumberAxis("Raw value");
		axis.setAutoRange(false);
		axis.setRange(min, max);
		
		NumberAxis axis2 = new NumberAxis("Calibrated value / N");
		axis2.setAutoRange(false);
		min = cal.toOutput(min);
		max = cal.toOutput(max);
		if (max < min) {
			max = min;
		}
		axis2.setRange(min, max);
		plot.setRangeAxes(new ValueAxis[] { axis, axis2 });
	}
	
	
	public void reset() {
		for (XYSeries s : series) {
			s.clear();
		}
		dataPoints.clear();
		pendingDataPoints.clear();
		
		dataPointCount = 0;
		dataErrorCount = 0;
		timingMissCount = 0;
		
		dataCount.setText("");
		dataRate.setText("");
		timingMisses.setText("");
		dataErrors.setText("");
	}
	
	
	@Override
	public void processData(List<DataPoint> data) {
		pendingDataPoints.addAll(data);
		dataPointCount += data.size();
		
		long t = System.currentTimeMillis();
		if (t >= nextFlushTime) {
			flushData();
			nextFlushTime = t + TIME_DELTA;
		}
	}
	
	private void flushData() {
		if (pendingDataPoints.isEmpty()) {
			return;
		}
		
		dataPoints.addAll(pendingDataPoints);
		
		double time = pendingDataPoints.get(0).getTime();
		double[] values = graphCreator.aggregate(pendingDataPoints);
		for (int i = 0; i < values.length; i++) {
			series[i].add(time, values[i]);
		}
		pendingDataPoints.clear();
		
		purgeOldData(time);
		computeStatistics();
	}
	
	private void computeStatistics() {
		double delta = dataPoints.getLast().getTime() - dataPoints.getFirst().getTime();
		int count = dataPoints.size();
		double rate = (count - 1) / delta;
		
		delta = (dataPoints.getLast().getTimestamp() - dataPoints.getFirst().getTimestamp()) / 1000.0;
		double actual = (count - 1) / delta;
		
		dataCount.setText("" + dataPointCount);
		dataRate.setText(String.format("%.1f Hz", rate));
		actualRate.setText(String.format("%.1f Hz", actual));
		timingMisses.setText("" + timingMissCount);
		dataErrors.setText("" + dataErrorCount);
	}
	
	private void purgeOldData(double time) {
		while (series[0].getX(0).doubleValue() < time - PLOT_RANGE) {
			for (int i = 0; i < series.length; i++) {
				series[i].remove(0);
			}
		}
		
		while (dataPoints.getFirst().getTime() < time - COMPUTE_RANGE) {
			dataPoints.removeFirst();
		}
	}
	
	@Override
	public void timingMiss() {
		timingMissCount++;
	}
	
	@Override
	public void dataError() {
		dataErrorCount++;
	}
	
}
