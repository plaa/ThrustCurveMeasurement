package tcm.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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
	private XYPlot plot;
	
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
		
		plot = (XYPlot) chart.getPlot();
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
		
		this.add(new JLabel("Click+drag lower-right to zoom in, upper-left to reset zoom"), "spanx, split, gapright para");
		
		
		final JCheckBox showPoints = new JCheckBox("Show points");
		showPoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((XYLineAndShapeRenderer) plot.getRenderer()).setBaseShapesVisible(showPoints.isSelected());
			}
		});
		this.add(showPoints, "right, wrap");
		
		
		this.add(new JPanel(), "growx");
		
	}
	
	public void update(Measurement original, Measurement filtered) {
		double tMin = filtered.getDataPoints().get(0).getTime();
		double tMax = filtered.getDataPoints().get(filtered.getDataPoints().size() - 1).getTime();
		
		updateSeries(originalSeries, original.getDataPoints(), tMin, tMax);
		updateSeries(filteredSeries, filtered.getDataPoints(), tMin, tMax);
	}
	
	private void updateSeries(XYSeries series, List<DataPoint> dataPoints, double tMin, double tMax) {
		series.setNotify(false);
		series.clear();
		for (DataPoint p : dataPoints) {
			if (p.getTime() >= tMin && p.getTime() <= tMax) {
				series.add(p.getTime(), p.getValue());
			}
		}
		series.setNotify(true);
	}
	
}
