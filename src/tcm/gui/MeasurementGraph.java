package tcm.gui;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.MathUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import tcm.data.DataPoint;
import tcm.document.Measurement;


public class MeasurementGraph extends JPanel {
	
	private static final Color FILTERED_COLOR = Color.BLUE;
	private static final Color ORIGINAL_COLOR = new Color(0.6f, 0.6f, 0.6f, 1.0f);
	
	private XYSeries filteredSeries;
	private XYSeries originalSeries;
	
	private JFreeChart chart;
	
	
	public MeasurementGraph() {
		super(new MigLayout("fill"));
		
		XYSeriesCollection collection = new XYSeriesCollection();
		filteredSeries = new XYSeries("Filtered");
		collection.addSeries(filteredSeries);
		originalSeries = new XYSeries("Original");
		collection.addSeries(originalSeries);
		
		
		// Create the chart using the factory to get all default settings
		chart = ChartFactory.createXYLineChart(
				null,
				"Time / s",
				"Thrust / N",
				collection,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.getRenderer().setSeriesPaint(0, FILTERED_COLOR);
		plot.getRenderer().setSeriesPaint(1, ORIGINAL_COLOR);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true, // save
				false, // print
				true, // zoom
				true); // tooltips
		
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		this.add(chartPanel, "span, grow, wrap rel");
		
		this.add(new JLabel("Click+drag lower-right to zoom in, upper-left to reset zoom"));
	}
	
	public void update(Measurement original, Measurement filtered) {
		updateSeries(originalSeries, original.getDataPoints());
		updateSeries(filteredSeries, filtered.getDataPoints());
	}
	
	private void updateSeries(XYSeries series, List<DataPoint> dataPoints) {
		series.clear();
		
		if (dataPoints.size() < 2) {
			return;
		}
		
		/*
		 * Sample data for every pixel to be drawn, and plot minimum and maximum values.
		 * Data is oversampled by a factor of 2.
		 */
		
		DataPoint first = dataPoints.get(0);
		DataPoint last = dataPoints.get(dataPoints.size() - 1);
		
		double deltaT = (last.getTime() - first.getTime()) / this.getWidth() / 2;
		
		double previous = first.getTime();
		double min = first.getValue();
		double max = first.getValue();
		for (DataPoint p : dataPoints) {
			if (p.getTime() > previous + deltaT) {
				series.add(previous, min);
				series.add(previous, max);
				previous = p.getTime();
				min = p.getValue();
				max = p.getValue();
			}
			
			min = MathUtil.min(min, p.getValue());
			max = MathUtil.max(max, p.getValue());
		}
		
		series.add(previous, min);
		series.add(previous, max);
	}
	
	
	
	
	
}
