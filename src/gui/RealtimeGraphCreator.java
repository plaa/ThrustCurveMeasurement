package gui;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import net.sf.openrocket.util.MathUtil;
import data.DataPoint;

public class RealtimeGraphCreator {
	
	private static final String[] NAMES = { "Min", "Avg", "Max" };
	private Paint[] PAINTS = { Color.BLUE, Color.RED, Color.BLUE };
	private static final int LENGTH = NAMES.length;
	
	public int getCount() {
		return LENGTH;
	}
	
	public String getName(int index) {
		return NAMES[index];
	}
	
	public Paint getPaint(int i) {
		return PAINTS[i];
	}
	
	public double[] aggregate(List<DataPoint> dataPoints) {
		double avg = 0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		int count = 0;
		for (DataPoint dp : dataPoints) {
			double value = dp.getValue();
			avg += value;
			min = MathUtil.min(min, value);
			max = MathUtil.max(max, value);
			count++;
		}
		avg /= count;
		return new double[] { min, avg, max };
	}
	
}
