package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import properties.PropertyList;
import properties.PropertyValue;
import properties.PropertyVisualizer;

public class PropertyTable extends JTable {
	private static final String[] COLUMN_NAMES = { "Name", "Value" };
	
	private final PropertyList properties;
	private final boolean editable;
	
	public PropertyTable(PropertyList properties, boolean editable) {
		this.properties = properties;
		this.editable = editable;
		
		this.setModel(new Model());
		this.setDefaultRenderer(PropertyValue.class, new Renderer());
	}
	
	
	private class Model extends AbstractTableModel {
		
		@Override
		public int getRowCount() {
			return properties.size();
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			} else {
				return PropertyValue.class;
			}
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}
		
		//		@Override
		//		public boolean isCellEditable(int rowIndex, int columnIndex) {
		//			return columnIndex == 1 && editable;
		//		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return properties.getName(rowIndex);
			} else {
				return properties.getValue(rowIndex);
			}
		}
		
		//		@Override
		//		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//			// TODO Auto-generated method stub
		//			
		//		}
		
	}
	
	private class Renderer implements TableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object objectValue, boolean isSelected, boolean hasFocus, int row, int column) {
			PropertyValue value = (PropertyValue) objectValue;
			PropertyVisualizer visualizer = value.getType().getRenderer();
			if (isSelected) {
				return visualizer.getComponent(value, getSelectionForeground(), getSelectionBackground());
			} else {
				return visualizer.getComponent(value, Color.BLACK, Color.WHITE);
			}
		}
		
	}
}
