package gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.util.Pair;
import seriallistener.Configuration;
import filter.DataAveragingFilter;

public class DataPresentationPanel extends JPanel {
	
	public static final long MAX_AGE = 10000000;
	
	private static final long[] TIMES = { 0, 100000, 1000000, 10000000 };
	
	private final int[] inputs;
	
	private final boolean fastmode;
	
	private JLabel[][] filteredValues;
	private JLabel[][] rawValues;
	
	private JLabel[] intervals;
	
	private JLabel extraInfo;
	
	public DataPresentationPanel(Configuration config) {
		super(new MigLayout("fillx, gap unrel para"));
		
		inputs = config.getInputArray().clone();
		final String[] names = config.getInputNamesArray();
		final int count = inputs.length;
		this.fastmode = config.getFastMode();
		
		this.add(new JLabel(""), "sg label, growx");
		this.add(new JLabel("Inst."), "sg label, growx");
		this.add(new JLabel("0.1 s"), "sg label, growx");
		this.add(new JLabel("1 s"), "sg label, growx");
		this.add(new JLabel("10 s"), "sg label, growx, wrap para");
		
		filteredValues = new JLabel[count][TIMES.length];
		rawValues = new JLabel[count][TIMES.length];
		intervals = new JLabel[TIMES.length];
		
		for (int i = 0; i < count; i++) {
			
			// Filtered values
			JLabel label;
			label = new StyledLabel(names[i] + " filt.", Style.BOLD);
			label.setToolTipText("Output value (converted)");
			this.add(label, "sg label, gap unrel");
			
			for (int j = 0; j < TIMES.length; j++) {
				filteredValues[i][j] = new StyledLabel(Style.BOLD);
				this.add(filteredValues[i][j], "sg label");
			}
			this.add(new JLabel(), "wrap 10lp");
			

			// Raw values
			label = new JLabel(names[i] + " raw");
			label.setToolTipText("Raw input value");
			this.add(label, "sg label, gap unrel");
			
			for (int j = 0; j < TIMES.length; j++) {
				rawValues[i][j] = new JLabel("");
				this.add(rawValues[i][j], "sg label");
			}
			this.add(new JLabel(), "wrap 20lp");
		}
		
		this.add(new JLabel("Interval (ms)"));
		for (int j = 0; j < TIMES.length; j++) {
			intervals[j] = new JLabel();
			this.add(intervals[j], "sg label");
		}
		this.add(new JLabel(), "wrap para");
		

		extraInfo = new JLabel(" ");
		this.add(extraInfo, "spanx");
	}
	
	
	public void update(DataAveragingFilter filter) {
		
		for (int i = 0; i < inputs.length; i++) {
			int n = inputs[i];
			
			for (int j = 0; j < TIMES.length; j++) {
				filteredValues[i][j].setText(format(filter.averageFilteredValues(n, TIMES[j])));
				rawValues[i][j].setText(format(filter.averageRawValues(n, TIMES[j])));
			}
		}
		
		for (int j = 0; j < TIMES.length; j++) {
			intervals[j].setText(formatTime(filter.averageInterval(TIMES[j])));
		}
		
		String extra = "";
		if (!fastmode) {
			extra += String.format("Missed bytes: %d   Timing misses: %d   ",
					filter.getByteMissCount(), filter.getTimingMissCount());
		}
		extra += String.format("Samples: %d   Buffer size: %d   Time: %d:%02d",
				filter.getSampleCount(), filter.getBufferSize(),
				filter.getTime() / 60000, (filter.getTime() / 1000) % 60);
		
		extraInfo.setText(extra);
	}
	
	
	private String formatTime(double time) {
		return String.format("%.3f", time / 1000.0);
		//		return format(time/1000.0, Double.NaN);
	}
	
	private String format(Pair<Double, Double> value) {
		return format(value.getU(), value.getV());
	}
	
	private String format(double avg, double std) {
		String a, s;
		
		if (avg >= 100) {
			a = String.format("%d", Math.round(avg));
			s = String.format("%d", Math.round(std));
		} else if (avg >= 10) {
			a = String.format("%.1f", avg);
			s = String.format("%.1f", std);
		} else {
			a = String.format("%.2f", avg);
			s = String.format("%.2f", std);
		}
		

		if (!Double.isNaN(std)) {
			return a + " \u00B1 " + s;
		} else {
			return a;
		}
	}
	
}
