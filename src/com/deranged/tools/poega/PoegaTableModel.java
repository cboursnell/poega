package com.deranged.tools.poega;

import javax.swing.table.DefaultTableModel;


public class PoegaTableModel extends DefaultTableModel {


	
	
	public PoegaTableModel(Object[][] data, String[] columnNames) {
		super(data, columnNames);
		// TODO Auto-generated constructor stub
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case 0:
			return false;
		case 1:
			return false;
		case 2:
			return true;
		default:
			return false;
		}
	}

}
