package tcm.startup;


import java.io.File;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.plugin.PluginModule;
import tcm.gui.MainDialog;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class Main {
	
	public static void main(String[] args) {
		
		// RXTX does not contain port definitions for /dev/ttyACMx
		String ports = makePortString(
				"/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3", "/dev/ttyUSB4", "/dev/ttyUSB5",
				"/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyACM3", "/dev/ttyACM4", "/dev/ttyACM5",
				"/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyS5",
				"COM1", "COM2", "COM3", "COM4", "COM5", "COM6");
		System.setProperty("gnu.io.rxtx.SerialPorts", ports);
		
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runInEdt();
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
	
	
	private static void runInEdt() {
		GUIUtil.setBestLAF();
		
		Module guiModule = new GuiModule();
		Module pluginModule = new PluginModule(null, Arrays.asList("net.sf.openrocket"));
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		injector.getInstance(MainDialog.class).setVisible(true);
	}
}
