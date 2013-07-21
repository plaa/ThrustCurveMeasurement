package tcm.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CSVReader {
	
	private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*[%#].*");
	
	public void readCSV(File file, CSVOutput output) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (COMMENT_PATTERN.matcher(line).matches()) {
					if (!output.comment(line)) {
						break;
					}
					continue;
				}
				
				String[] parts = line.split(",", Integer.MAX_VALUE);
				if (!output.line(Arrays.asList(parts))) {
					break;
				}
			}
		} finally {
			reader.close();
		}
	}
	
	public static interface CSVOutput {
		/**
		 * Handle the result of a single CSV line.
		 * 
		 * @param values	the values on the line
		 * @return			true to continue, false to stop
		 */
		public boolean line(List<String> values);
		
		/**
		 * Handle a comment line in the CSV file.
		 * 
		 * @param comment	the comment line
		 * @return			true to continue, false to stop
		 */
		public boolean comment(String comment);
	}
}
