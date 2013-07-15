package filter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import arduinoad.ArduinoConfiguration;
import arduinoad.DataVO;

import net.sf.openrocket.util.TextUtil;

public class DataSaveFilter extends DataFilter {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	private PrintWriter stream = null;
	private ArduinoConfiguration config = null;
	
	
	public void startSave(File file, ArduinoConfiguration config, String comment) throws IOException {
		
		stopSave();
		
		stream = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file), 10000), false);
		this.config = config;
		writeHeader(comment);
		
	}

	public void stopSave() {
		if (stream == null)
			return;
		
		stream.flush();
		stream.close();
		stream = null;
	}
	
	
	private void writeHeader(String comment) {
		
		// Write comment
		if (comment != null) {
			String[] split = comment.split("\n");
			for (String s: split) {
				if (!s.startsWith("#")) {
					s = "# " + s;
				}
				stream.println(s);
			}
			stream.println("#");
		}
		
		// Write time stamp
		stream.println("# Save started on " + DATE_FORMAT.format(new Date()));
		stream.println("#");
		stream.print("# Sample no, timestamp (us)");
		
		for (String name: config.getInputNamesArray()) {
			stream.print(", " + name + " (filtered), " + name + " (raw)");
		}
		stream.println();
		
	}
	
	
	@Override
	protected void filter(DataVO data) {
		if (stream == null)
			return;
		
		if (data.isByteMissObject()) {
			stream.println("# Byte miss");
			return;
		}
		
		stream.print((this.getSampleCount() + this.getByteMissCount()));
		stream.print(',');
		stream.print(data.getTimeStamp());
		for (int n: config.getInputArray()) {
			stream.print(',');
			stream.print(TextUtil.doubleToString(data.getFilteredValue(n)));
			stream.print(',');
			stream.print(data.getRawValue(n));
		}
		stream.println();

	}
	
	

}
