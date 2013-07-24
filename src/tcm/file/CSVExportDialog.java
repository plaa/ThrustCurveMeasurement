package tcm.file;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import tcm.data.DataPoint;
import tcm.defaults.Defaults;
import tcm.document.Measurement;
import tcm.properties.PropertyValue;
import tcm.properties.ProperyNames;

import com.google.inject.Inject;

public class CSVExportDialog extends JDialog {
	
	private static final String SPACE = "SPACE";
	private static final String TAB = "TAB";
	private static final String[] DEFAULT_COMMENT_CHARS = { "#", "%" };
	private static final String[] DEFAULT_SEPARATOR_CHARS = { ",", ";", SPACE, TAB };
	
	private JComboBox commentChar;
	private JComboBox separatorChar;
	
	private JTextArea comment;
	
	private boolean canceled = true;
	
	private Defaults defaults;
	
	@Inject
	public CSVExportDialog(Defaults defaults) {
		super(null, "Export CSV", ModalityType.APPLICATION_MODAL);
		this.defaults = defaults;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel("Comment character:"), "");
		commentChar = new JComboBox(DEFAULT_COMMENT_CHARS);
		commentChar.setEditable(true);
		commentChar.setSelectedItem(defaults.getString("CSVExport.commentChar", DEFAULT_COMMENT_CHARS[0]));
		panel.add(commentChar, "wrap para");
		
		panel.add(new JLabel("Separator character:"), "");
		separatorChar = new JComboBox(DEFAULT_SEPARATOR_CHARS);
		separatorChar.setEditable(true);
		separatorChar.setSelectedItem(defaults.getString("CSVExport.separatorChar", DEFAULT_SEPARATOR_CHARS[0]));
		panel.add(separatorChar, "wrap para");
		
		panel.add(new JLabel("Comment:"), "wrap rel");
		comment = new JTextArea(5, 40);
		panel.add(comment, "spanx, grow, wrap para");
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				setVisible(false);
			}
		});
		panel.add(ok, "spanx, split, right");
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				setVisible(false);
			}
		});
		panel.add(cancel, "right");
		
		this.add(panel);
		
		canceled = true;
		GUIUtil.setDisposableDialogOptions(this, ok);
	}
	
	public boolean export(File file, Measurement measurement) throws IOException {
		PropertyValue commentValue = measurement.getPropertyList().getValue(ProperyNames.COMMENT);
		String cmt;
		if (commentValue != null) {
			cmt = ((String) commentValue.getValue()).trim();
		} else {
			cmt = "";
		}
		if (cmt.length() > 0) {
			cmt = cmt + "\n";
		}
		cmt = cmt + "Time,Measurement timestamp,Value";
		comment.setText(cmt);
		
		this.setVisible(true);
		
		if (canceled) {
			return false;
		}
		
		String commentStart = (String) commentChar.getSelectedItem();
		String separator = (String) separatorChar.getSelectedItem();
		cmt = comment.getText().trim();
		
		defaults.putString("CSVExport.commentChar", commentStart);
		defaults.putString("CSVExport.separatorChar", separator);
		
		FileWriter writer = new FileWriter(file);
		try {
			if (cmt.length() > 0) {
				for (String s : cmt.split("\n")) {
					writer.write(commentStart + " " + s + "\n");
				}
			}
			
			for (DataPoint p : measurement.getDataPoints()) {
				writer.write(p.getTime() + separator + p.getTimestamp() + separator + p.getValue() + "\n");
			}
		} finally {
			writer.close();
		}
		
		return true;
	}
	
}
