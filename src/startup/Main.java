package startup;

import gui.MainDialog;

import java.util.Arrays;

import javax.swing.SwingUtilities;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.plugin.PluginModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runInEdt();
			}
		});
	}
	
	private static void runInEdt() {
		GUIUtil.setBestLAF();
		
		Module guiModule = new GuiModule();
		Module pluginModule = new PluginModule(null, Arrays.asList("net.sf.openrocket"));
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		injector.getInstance(MainDialog.class).setVisible(true);
	}
}
