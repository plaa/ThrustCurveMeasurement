package gui;

import static seriallistener.SerialDataCommunicator.INPUTS;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.unit.GeneralUnit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import seriallistener.Configuration;
import seriallistener.DataVO;
import seriallistener.MockDataCommunicator;
import seriallistener.SerialDataCommunicator;
import util.Interpolator;
import filter.DataAveragingFilter;
import filter.DataInterpolationFilter;
import filter.DataSaveFilter;


public class DataAnalyzer extends JFrame {
	
	private static final boolean MOCK = false;
	
	public static final UnitGroup UNITS_DELAY;
	static {
		UNITS_DELAY = new UnitGroup();
		UNITS_DELAY.addUnit(new GeneralUnit(1, "s"));
		UNITS_DELAY.addUnit(new GeneralUnit(0.001, "ms"));
		UNITS_DELAY.addUnit(new GeneralUnit(0.000001, Chars.MICRO + "s"));
		UNITS_DELAY.setDefaultUnit(2);
	}
	
	private static final int CALIBRATION_TIME = 5000000; // 5 sec
	
	private static final int UPDATE_DELAY = 300;
	
	private static final Configuration DEFAULT_CONFIGURATION =
			new Configuration(
					"/dev/ttyUSB0", // device
					115200, // device speed
					1000, // read delay (us)
					false, // external reference
					false, // fast mode
					new int[] { 0 }, // inputs
					new String[] { "Input 0" }
			);
	
	private final SerialDataCommunicator communicator;
	private final BlockingQueue<DataVO> dataQueue;
	private final DataInterpolationFilter interpolationFilter;
	private final DataAveragingFilter averagingFilter;
	private final DataSaveFilter saveFilter;
	
	
	private Configuration configuration = null;
	
	private PlotDialog plotDialog = null;
	
	private final JSplitPane splitPane;
	private final JToggleButton startButton;
	private final JTextArea commentTextArea;
	private final JCheckBox saveDataButton;
	private final JTextField saveFileField;
	
	private boolean running = false;
	
	private DataPresentationPanel presentationPanel = null;
	
	
	private final List<Interpolator> interpolators;
	
	
	
	public DataAnalyzer() {
		super("Serial data analyzer");
		
		if (MOCK) {
			communicator = new MockDataCommunicator();
		} else {
			communicator = new SerialDataCommunicator();
		}
		dataQueue = communicator.getQueue();
		
		interpolationFilter = new DataInterpolationFilter();
		List<Interpolator> list = new ArrayList<Interpolator>();
		for (int i = 0; i < INPUTS; i++) {
			Interpolator interp = new Interpolator(0, 0, 1, 1);
			list.add(interp);
			interpolationFilter.setInterpolator(i, interp);
		}
		interpolators = Collections.unmodifiableList(list);
		
		//		communicator.addDataListener(interpolationFilter, true);
		
		averagingFilter = new DataAveragingFilter(10000000); // 10 sec
		interpolationFilter.addDataListener(averagingFilter);
		
		saveFilter = new DataSaveFilter();
		interpolationFilter.addDataListener(saveFilter);
		
		
		
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		
		//		JButton test = new JButton("Test");
		//		test.addActionListener(new ActionListener() {
		//			@Override
		//			public void actionPerformed(ActionEvent e) {
		//				try {
		//					SerialDataCommunicator.main(null);
		//				} catch (Exception e1) {
		//					e1.printStackTrace();
		//				}
		//			}
		//		});
		//		panel.add(test,"spanx, split");
		
		
		final JButton configureButton = new JButton("Configure");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configure();
			}
		});
		panel.add(configureButton, "spanx, split, gapright para");
		
		startButton = new JToggleButton("Start listening");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (startButton.isSelected()) {
					start();
				} else {
					stop();
				}
			}
		});
		panel.add(startButton, "gapright para");
		
		final JButton plotButton = new JButton("Plot");
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closePlotDialog();
				plotDialog = new PlotDialog(DataAnalyzer.this, configuration);
				interpolationFilter.addDataListener(plotDialog);
				plotDialog.setVisible(true);
			}
		});
		panel.add(plotButton, "");
		
		panel.add(new JPanel(), "growx, wrap para");
		
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(new JPanel());
		splitPane.setRightComponent(new JPanel());
		splitPane.setDividerLocation(0.4);
		splitPane.setResizeWeight(0.4);
		splitPane.setContinuousLayout(true);
		panel.add(splitPane, "spanx, grow, wrap");
		
		
		
		
		commentTextArea = new JTextArea(4, 40);
		commentTextArea.setToolTipText("Comment to write at the beginning of the saved data file");
		panel.add(new JScrollPane(commentTextArea), "spanx, split");
		
		
		JPanel sub = new JPanel(new MigLayout("fill"));
		
		saveDataButton = new JCheckBox("Save data to file:");
		saveDataButton.setToolTipText("Begin saving data to a file");
		saveDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (saveDataButton.isSelected()) {
					String str = saveFileField.getText().trim();
					if (str.length() == 0) {
						JFileChooser chooser = new JFileChooser((File) null);
						if (chooser.showDialog(DataAnalyzer.this, "OK") != JFileChooser.APPROVE_OPTION)
							return;
						
						str = chooser.getSelectedFile().getPath();
						saveFileField.setText(str);
					}
					File file = new File(str);
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(DataAnalyzer.this,
								"Overwrite " + file.getName() + "?", "Overwrite file",
								JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							return;
						}
					}
					try {
						saveFilter.startSave(file, configuration, commentTextArea.getText());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(DataAnalyzer.this,
								"Error opening file: " + e1.getMessage(), "I/O error",
								JOptionPane.ERROR_MESSAGE);
						saveDataButton.setSelected(false);
					}
				} else {
					saveFilter.stopSave();
				}
			}
		});
		sub.add(saveDataButton, "spanx, growx 1, wrap para");
		
		
		saveFileField = new JTextField();
		sub.add(saveFileField, "growx 1");
		
		JButton button = new JButton("Browse");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file;
				String text = saveFileField.getText();
				if (text.trim().length() == 0)
					file = null;
				else
					file = new File(text);
				
				JFileChooser chooser = new JFileChooser(file);
				if (chooser.showDialog(DataAnalyzer.this, "OK") == JFileChooser.APPROVE_OPTION) {
					saveFileField.setText(chooser.getSelectedFile().getPath());
				}
			}
		});
		sub.add(button);
		
		panel.add(sub, "growx 1");
		
		
		
		
		
		this.add(panel);
		
		setConfiguration(getDefaultConfiguration());
		
		
		// Cause all plot dialog classes to load from JFreeChart
		new PlotDialog(this, configuration);
		
		
		ActionListener timer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleData();
			}
		};
		new Timer(UPDATE_DELAY, timer).start();
		
		this.pack();
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width = size.width * 8 / 10;
		size.height = size.height * 7 / 10;
		this.setSize(size);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationByPlatform(true);
	}
	
	
	private void closePlotDialog() {
		if (plotDialog != null) {
			interpolationFilter.removeDataListener(plotDialog);
			plotDialog.setVisible(false);
			plotDialog.dispose();
			plotDialog = null;
		}
	}
	
	
	private void configure() {
		if (startButton.isSelected()) {
			startButton.setSelected(false);
		}
		
		JPanel panel = new JPanel(new MigLayout("fillx"));
		
		panel.add(new JLabel("Serial port:"));
		JComboBox portSelector = new JComboBox(communicator.getSerialPorts());
		portSelector.setEditable(true);
		if (configuration.getSerialDevice().trim().length() == 0) {
			if (portSelector.getItemCount() > 0)
				portSelector.setSelectedIndex(0);
		} else {
			portSelector.setSelectedItem(configuration.getSerialDevice());
		}
		panel.add(portSelector, "spanx, wrap");
		
		panel.add(new JLabel("Serial speed:"));
		JComboBox speedSelector = new JComboBox(new String[] {
				"9600", "38400", "57600", "115200", "230400", "460800"
		});
		speedSelector.setEditable(true);
		speedSelector.setSelectedItem("" + configuration.getSerialSpeed());
		panel.add(speedSelector, "spanx, wrap para");
		
		
		panel.add(new JLabel("Sampling delay:"));
		DoubleValue delay = new DoubleValue(configuration.getDelay() / 1000000.0);
		DoubleModel dm = new DoubleModel(delay, "Value", UNITS_DELAY, 0);
		panel.add(new JSpinner(dm.getSpinnerModel()), "width 100lp, spanx, split");
		panel.add(new UnitSelector(dm), "wrap para");
		
		
		int[] inputs = configuration.getInputArray();
		String[] inputNames = configuration.getInputNamesArray();
		
		JCheckBox[] inputSelectors = new JCheckBox[INPUTS];
		JTextField[] inputNameSelectors = new JTextField[INPUTS];
		for (int i = 0; i < INPUTS; i++) {
			int index;
			
			panel.add(new JLabel("Input " + i + ":"));
			
			inputSelectors[i] = new JCheckBox();
			inputNameSelectors[i] = new JTextField();
			index = Arrays.binarySearch(inputs, i);
			if (index >= 0) {
				inputSelectors[i].setSelected(true);
				inputNameSelectors[i].setText(inputNames[index]);
			} else {
				inputSelectors[i].setSelected(false);
				inputNameSelectors[i].setText("Input " + i);
			}
			
			panel.add(inputSelectors[i]);
			panel.add(inputNameSelectors[i], "spanx, growx, wrap");
		}
		
		JCheckBox external = new JCheckBox("Use external Aref");
		external.setSelected(configuration.getExternalReference());
		panel.add(external, "gaptop para, skip, spanx, wrap");
		
		JCheckBox fastmode = new JCheckBox("Fast 8-bit mode");
		fastmode.setSelected(configuration.getFastMode());
		panel.add(fastmode, "skip, spanx, wrap");
		
		
		while (true) {
			int sel = JOptionPane.showConfirmDialog(this, panel, "Configuration",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (sel != JOptionPane.OK_OPTION)
				return;
			
			
			String serialPort = ((String) portSelector.getSelectedItem()).trim();
			if (serialPort.length() == 0) {
				JOptionPane.showMessageDialog(this, "Serial port not selected", "Error",
						JOptionPane.ERROR_MESSAGE);
				continue;
			}
			
			int serialSpeed;
			try {
				serialSpeed = Integer.parseInt((String) speedSelector.getSelectedItem());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Invalid serial speed", "Error",
						JOptionPane.ERROR_MESSAGE);
				continue;
			}
			if (serialSpeed % 100 != 0) {
				JOptionPane.showMessageDialog(this, "Serial speed must be a multiple of 100.",
						"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			
			int delayMicros = (int) (delay.getValue() * 1000000 + 0.5);
			
			boolean externalRef = external.isSelected();
			
			boolean fast = fastmode.isSelected();
			
			ArrayList<Integer> selectedInputs = new ArrayList<Integer>();
			ArrayList<String> selectedInputNames = new ArrayList<String>();
			for (int i = 0; i < INPUTS; i++) {
				if (inputSelectors[i].isSelected()) {
					selectedInputs.add(i);
					selectedInputNames.add(inputNameSelectors[i].getText().trim());
				}
			}
			if (selectedInputs.size() == 0) {
				JOptionPane.showMessageDialog(this, "At least one input must be selected.",
						"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			if (fast && selectedInputs.size() != 1) {
				JOptionPane.showMessageDialog(this, "Only one input allowed in fast 8-bit mode.",
						"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			
			int[] intArray = new int[selectedInputs.size()];
			for (int i = 0; i < selectedInputs.size(); i++) {
				intArray[i] = selectedInputs.get(i);
			}
			
			
			Configuration newConfig = new Configuration(serialPort, serialSpeed, delayMicros,
					externalRef, fast, intArray, selectedInputNames.toArray(new String[0]));
			setConfiguration(newConfig);
			setDefaultConfiguration(newConfig);
			break;
		}
		
	}
	
	private void start() {
		if (running)
			return;
		
		try {
			communicator.startListener(configuration);
			
			averagingFilter.reset();
			running = true;
			this.startButton.setSelected(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					new Object[] {
							"Error starting serial listener:",
							e.getMessage() }, "Error starting",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			running = false;
			this.startButton.setSelected(false);
		}
	}
	
	private void stop() {
		if (saveDataButton.isSelected()) {
			saveDataButton.setSelected(false);
		}
		
		if (!running)
			return;
		
		communicator.stopListener();
		running = false;
		this.startButton.setSelected(false);
	}
	
	
	private void handleData() {
		if (!running)
			return;
		
		List<DataVO> list = new LinkedList<DataVO>();
		dataQueue.drainTo(list);
		interpolationFilter.processData(list);
		
		if (presentationPanel != null) {
			presentationPanel.update(averagingFilter);
		}
		
		// Ensure that other threads get some time
		try {
			Thread.sleep(1);
		} catch (InterruptedException ignore) {
		}
	}
	
	
	private void setConfiguration(Configuration configuration) {
		closePlotDialog();
		
		this.configuration = configuration;
		
		presentationPanel = new DataPresentationPanel(configuration);
		splitPane.setRightComponent(presentationPanel);
		
		JTabbedPane tabbed = new JTabbedPane();
		String[] names = configuration.getInputNamesArray();
		int[] inputs = configuration.getInputArray();
		for (int i = 0; i < inputs.length; i++) {
			int input = inputs[i];
			tabbed.addTab(names[i], new InterpolatorEditorPane(interpolators.get(input),
					new AverageCalibrator(input)));
		}
		splitPane.setLeftComponent(tabbed);
	}
	
	
	private void setDefaultConfiguration(Configuration configuration) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(configuration);
			oos.flush();
			oos.close();
			
			byte[] array = baos.toByteArray();
			Preferences.userRoot().node("SerialDataReceiver").putByteArray("defaultConfiguration", array);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Configuration getDefaultConfiguration() {
		try {
			byte[] array = Preferences.userRoot().node("SerialDataReceiver").getByteArray("defaultConfiguration", null);
			if (array == null) {
				return DEFAULT_CONFIGURATION;
			}
			
			ByteArrayInputStream bais = new ByteArrayInputStream(array);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Configuration config = (Configuration) ois.readObject();
			return config;
		} catch (Exception e) {
			System.err.println("Could not read stored default configuration.");
			e.printStackTrace();
			return DEFAULT_CONFIGURATION;
		}
	}
	
	private class AverageCalibrator implements Calibrator {
		private final int input;
		
		public AverageCalibrator(int input) {
			this.input = input;
		}
		
		@Override
		public double getCalibrationValue() {
			return averagingFilter.averageRawValues(input, CALIBRATION_TIME).getU();
		}
	}
	
	
	public static void main(String[] arg) throws InterruptedException, InvocationTargetException {
		
		// RXTX does not contain port definitions for /dev/ttyACMx
		String ports = makePortString(
				"/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3", "/dev/ttyUSB4", "/dev/ttyUSB5",
				"/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyACM3", "/dev/ttyACM4", "/dev/ttyACM5",
				"/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyS5",
				"COM1", "COM2", "COM3", "COM4", "COM5", "COM6");
		System.setProperty("gnu.io.rxtx.SerialPorts", ports);
		
		
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				runMain();
			}
		});
	}
	
	private static String makePortString(String... args) {
		String str = "";
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				str += File.pathSeparator;
			}
			str += args[i];
		}
		return str;
	}
	
	
	private static void runMain() {
		
		/*
		 * Set the look-and-feel.  On Linux, Motif/Metal is sometimes incorrectly used 
		 * which is butt-ugly, so if the system l&f is Motif/Metal, we search for a few
		 * other alternatives.
		 */
		try {
			// Set system L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Check whether we have an ugly L&F
			LookAndFeel laf = UIManager.getLookAndFeel();
			if (laf == null ||
					laf.getName().matches(".*[mM][oO][tT][iI][fF].*") ||
					laf.getName().matches(".*[mM][eE][tT][aA][lL].*")) {
				
				// Search for better LAF
				UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
				String lafNames[] = {
						".*[gG][tT][kK].*",
						".*[wW][iI][nN].*",
						".*[mM][aA][cC].*",
						".*[aA][qQ][uU][aA].*",
						".*[nN][iI][mM][bB].*"
				};
				
				lf: for (String lafName : lafNames) {
					for (UIManager.LookAndFeelInfo l : info) {
						if (l.getName().matches(lafName)) {
							UIManager.setLookAndFeel(l.getClassName());
							break lf;
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error setting LAF: " + e);
		}
		
		// Create and show frame
		DataAnalyzer frame = new DataAnalyzer();
		frame.setVisible(true);
	}
	
}
