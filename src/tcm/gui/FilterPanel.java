package tcm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.util.StateChangeListener;
import tcm.document.MeasurementDocument;
import tcm.filter.DataFilter;
import tcm.filter.DataFilterPlugin;

import com.google.inject.Inject;

public class FilterPanel extends JPanel {
	
	private List<DataFilterPlugin> availableFilters = new ArrayList<DataFilterPlugin>();
	
	private JList filterList;
	private Model listModel;
	
	private DescriptionArea description;
	private JScrollPane configuration;
	
	private MeasurementDocument document = new MeasurementDocument();
	
	@Inject
	public FilterPanel(Set<DataFilterPlugin> allFilters) {
		super(new MigLayout("fill"));
		
		availableFilters.addAll(allFilters);
		Collections.sort(availableFilters, new Comparator<DataFilterPlugin>() {
			@Override
			public int compare(DataFilterPlugin o1, DataFilterPlugin o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		this.add(new StyledLabel("Filters:", Style.BOLD), "wrap rel");
		
		listModel = new Model();
		document.addChangeListener(listModel);
		filterList = new JList(listModel);
		filterList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSelection();
			}
		});
		this.add(new JScrollPane(filterList), "spanx, growx, h 150lp, wrap para");
		
		
		final JPopupMenu popup = new JPopupMenu();
		for (final DataFilterPlugin plugin : availableFilters) {
			popup.add(new JMenuItem(new AbstractAction(plugin.getName()) {
				public void actionPerformed(ActionEvent e) {
					document.getFilters().add(plugin.getInstance());
					filterList.setSelectedIndex(document.getFilters().size() - 1);
					updateSelection();
				}
			}));
		}
		
		
		JButton button = new JButton("Add");
		button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		this.add(button, "spanx, split");
		
		button = new JButton("Up");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = filterList.getSelectedIndex();
				if (index < 1 || index > listModel.getSize() - 1) {
					return;
				}
				DataFilter f = document.getFilters().remove(index);
				document.getFilters().add(index - 1, f);
				filterList.setSelectedIndex(index - 1);
				updateSelection();
			}
		});
		this.add(button);
		
		button = new JButton("Down");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = filterList.getSelectedIndex();
				if (index < 0 || index > listModel.getSize() - 2) {
					return;
				}
				DataFilter f = document.getFilters().remove(index);
				document.getFilters().add(index + 1, f);
				filterList.setSelectedIndex(index + 1);
				updateSelection();
			}
		});
		this.add(button);
		
		button = new JButton("Remove");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = filterList.getSelectedIndex();
				if (index < 0 || index > listModel.getSize() - 1) {
					return;
				}
				document.getFilters().remove(index);
				updateSelection();
			}
		});
		this.add(button, "wrap para");
		
		
		this.add(new StyledLabel("Filter options:", Style.BOLD), "spanx, wrap rel");
		
		description = new DescriptionArea(3);
		description.setOpaque(false);
		this.add(description, "growx, wrap rel");
		
		configuration = new JScrollPane();
		configuration.setOpaque(false);
		this.add(configuration, "height 200lp, grow 1000");
		
		
	}
	
	public void setDocument(MeasurementDocument document) {
		this.document.removeChangeListener(listModel);
		this.document = document;
		this.document.addChangeListener(listModel);
		listModel.stateChanged(null);
	}
	
	
	
	private void updateSelection() {
		int index = filterList.getSelectedIndex();
		if (index < 0 || index >= document.getFilters().size()) {
			description.setText("");
			configuration.setViewportView(null);
			return;
		}
		
		DataFilter filter = document.getFilters().get(index);
		description.setText(filter.getDescription());
		configuration.setViewportView(filter.getConfigurationComponent());
	}
	
	
	private class Model extends AbstractListModel implements StateChangeListener {
		@Override
		public Object getElementAt(int index) {
			return document.getFilters().get(index).getName();
		}
		
		@Override
		public int getSize() {
			return document.getFilters().size();
		}
		
		@Override
		public void stateChanged(EventObject arg0) {
			fireContentsChanged(this, 0, getSize());
		}
	}
	
}
