package gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.inject.Inject;

import data.DataPoint;
import data.MeasurementListener;

public class RealtimeGraph extends JPanel implements MeasurementListener {
	
	private static final long TIME_DELTA = 100;
	private static final double PLOT_RANGE = 10;
	
	private RealtimeGraphCreator graphCreator;
	private XYSeries[] series;
	
	private List<DataPoint> pendingDataPoints = new ArrayList<DataPoint>();
	private long nextFlushTime = 0;
	
	private JFreeChart chart;
	
	@Inject
	public RealtimeGraph(RealtimeGraphCreator graphCreator) {
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
		
		this.add(chartPanel);
	}
	
	public void setRange(double min, double max) {
		XYPlot plot = (XYPlot) chart.getPlot();
		NumberAxis axis = new NumberAxis("Value");
		axis.setAutoRange(false);
		axis.setRange(min, max);
		plot.setRangeAxis(axis);
	}
	
	@Override
	public void processData(List<DataPoint> data) {
		pendingDataPoints.addAll(data);
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
		
		double time = pendingDataPoints.get(0).getTime() / 1000.0;
		double[] values = graphCreator.aggregate(pendingDataPoints);
		for (int i = 0; i < values.length; i++) {
			series[i].add(time, values[i]);
		}
		pendingDataPoints.clear();
		
		while (series[0].getX(0).doubleValue() < time - PLOT_RANGE) {
			for (int i = 0; i < series.length; i++) {
				series[i].remove(0);
			}
		}
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
