package com.deranged.tools.poega;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;

public class Main {
			
	// TODO add crossover operations to the Build class
	//      how would this work? 
	//         pick a random point in both strings of keynodes and swap after that point
	
	// TODO design a selection method
	//      tournament selection
	
	// TODO niching?
	//      cluster the builds based on some similarity score and try to discourage clustering      

	private JFrame     frame;
	private ViewPanel    panel;
	//private JPanel         panel;
	private JPanel         sidepanel;
	private JScrollPane      scrollPane;
	private JTable             table;
	
	private Model model;
	
	private int dragX;
	private int dragY;
	private int startX;
	private int startY;
	private int x;
	private int y;
	
	private DefaultComboBoxModel<Mod> cbModel;
	private PoegaTableModel tblModel;
	private DefaultTableColumnModel colModel;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		model = new Model();
		initialize();
		//model.init();
	}
	
	public final void addTarget(Mod mod) {
		

		boolean alreadyThere=false;
		int rowCount = tblModel.getRowCount();
		for (int r = 0; r < rowCount; r++) {
			if ((String) tblModel.getValueAt(r, 1) == mod.getDesc()) {
				alreadyThere=true;
			}
		}
		if (!alreadyThere) {
			model.addTarget(mod);
			cbModel.removeElement(mod);
			
			tblModel.setRowCount(0);
			
			Target target = model.getTarget();
			int count = target.getNumberOfMods();
			for(int i = 0; i < count; i++) {
				Mod tmp = target.getMod(i);
				Object[] rowData = {tmp.getPrefix()+tmp.getValue()+tmp.getPostfix(), tmp.getDesc(), target.getWeight(i)};
				tblModel.addRow(rowData);
			}
			tblModel.fireTableDataChanged();		
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				panel.repaint();
			}
		});
		//frame.setBounds(0, 0, model.getFrameWidth(), model.getFrameHeight());
		frame.setBounds(-1800, 0, 1800, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new ViewPanel(model);
		//panel = new JPanel();
		
		sidepanel = new JPanel();

		scrollPane = new JScrollPane();
		
		
		JLabel lblTargets = new JLabel("Targets:");
		
		Object[][] data = {{"", " no targets set", ""}};
		String[] columnNames = {"_", "_", "_"};
		
		tblModel = new PoegaTableModel(data, columnNames);        // do i have to make my own table model class extend AbstractTableModel?
		colModel = new DefaultTableColumnModel();
		
		table = new JTable(tblModel, colModel); //  construct new table here with PoegaTableModel and DefaultTableColumnModel

		table.setColumnSelectionAllowed(false);
		//table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		colModel.addColumn(new TableColumn(0));
		colModel.addColumn(new TableColumn(1));
		colModel.addColumn(new TableColumn(2));
		colModel.getColumn(0).setPreferredWidth(45);
		colModel.getColumn(1).setPreferredWidth(275);
		colModel.getColumn(2).setPreferredWidth(33);
		colModel.getColumn(0).setHeaderValue("value");
		colModel.getColumn(1).setHeaderValue("desc");
		colModel.getColumn(2).setHeaderValue("weight");
		//System.out.println("Table columns = " + colModel.getColumnCount());

		cbModel = new DefaultComboBoxModel<Mod>();
		Iterator<Mod> iter = model.getModTotals().iterator();
		//System.out.println("loading desc from model set");
		//System.out.println("size:"+model.getModTotals().size());
		
		while(iter.hasNext()) {
			Mod mod = iter.next();
			cbModel.addElement(mod);
		}

		final JComboBox<Mod> comboBox = new JComboBox<Mod>(cbModel);

		JButton btnAddTarget    = new JButton("Add Target");
		JButton btnEditTarget   = new JButton("Edit Target");
		JButton btnDeleteTarget = new JButton("Delete Target");
		
		btnEditTarget.setEnabled(false);
		
		JButton btnGo           = new JButton("Go");
		
		textField = new JTextField();


		textField.setColumns(10);
		
		// LAYOUT STUFF
		
		GroupLayout gl_sidepanel = new GroupLayout(sidepanel);
		gl_sidepanel.setHorizontalGroup(
			gl_sidepanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_sidepanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_sidepanel.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 353, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTargets)
						.addGroup(gl_sidepanel.createSequentialGroup()
							.addComponent(btnAddTarget)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnEditTarget)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDeleteTarget))
						.addComponent(btnGo)
						.addComponent(comboBox, 0, 380, Short.MAX_VALUE)
						.addComponent(textField, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_sidepanel.setVerticalGroup(
			gl_sidepanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_sidepanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblTargets)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 202, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_sidepanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAddTarget)
						.addComponent(btnEditTarget)
						.addComponent(btnDeleteTarget))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 594, Short.MAX_VALUE)
					.addComponent(btnGo)
					.addContainerGap())
		);
		
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1344, Short.MAX_VALUE)
					.addGap(10)
					.addComponent(sidepanel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(sidepanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 940, Short.MAX_VALUE)
						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 940, Short.MAX_VALUE))
					.addGap(10))
		);
		
		scrollPane.setViewportView(table);
		sidepanel.setLayout(gl_sidepanel);
		frame.getContentPane().setLayout(groupLayout);
		
		// EVENT HANDLERS

		panel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				model.addScale(e.getPreciseWheelRotation()*0.01f, e.getX(), e.getY());
				panel.repaint();
			}
		});
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				dragX = (int)(e.getX());
				dragY = (int)(e.getY());
				model.addPanX(dragX-startX);
				startX=dragX;
				model.addPanY(dragY-startY);

				startY=dragY;
				panel.repaint();
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				x = (int)((e.getX()-model.getPanX())/model.getScale() - model.getCamX());
				y = (int)((e.getY()-model.getPanY())/model.getScale() - model.getCamY());
				model.hover(x, y);
				//System.out.println("hover at "+x+" "+y);
				panel.repaint();
			}
		});
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startX = (int)(e.getX());
				startY = (int)(e.getY());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
		});
		
		btnAddTarget.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				addTarget((Mod)comboBox.getSelectedItem());
			}
		});
		
		btnEditTarget.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			}
		});
		
		btnDeleteTarget.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				model.deleteTarget(selectedRow);
				tblModel.removeRow(selectedRow);
				cbModel.removeAllElements();
				Iterator<Mod> iter = model.getModTotals().iterator();					
				while(iter.hasNext()) {
					Mod mod = iter.next();
					cbModel.addElement(mod);
				}
			}
		});
		
		btnGo.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				model.init();
				for (int g = 0 ; g < 15; g++) {
					model.generation();
					panel.repaint();
				}
			}
		});
		
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				// when return is hit
			}
		});
		
		textField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				String text = textField.getText();
				if (text.length()>2) {
					cbModel.removeAllElements();
					Iterator<Mod> iter = model.getModTotals().iterator();					
					while(iter.hasNext()) {
						Mod mod = iter.next();
						String regex = "(?i).*"+text+".*"; // (?i) makes the regex case insensitive
						if (mod.getDesc().matches(regex) || mod.getName().matches(regex)) {
							boolean alreadyThere=false;
							int rowCount = tblModel.getRowCount();
							for (int r = 0; r < rowCount; r++) {
								if ((String) tblModel.getValueAt(r, 1) == mod.getDesc()) {
									alreadyThere=true;
								}
							}
							if (!alreadyThere) {
								cbModel.addElement(mod);
							}
						} 
					}
				} else {
					cbModel.removeAllElements();
					Iterator<Mod> iter = model.getModTotals().iterator();					
					while(iter.hasNext()) {
						Mod mod = iter.next();
						boolean alreadyThere=false;
						int rowCount = tblModel.getRowCount();
						for (int r = 0; r < rowCount; r++) {
							if ((String) tblModel.getValueAt(r, 1) == mod.getDesc()) {
								alreadyThere=true;
							}
						}
						if (!alreadyThere) {
							cbModel.addElement(mod);
						}
					}
				}
			}
		});
		
		tblModel.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getFirstRow()>=0 && e.getColumn()>=0) {
					//System.out.print("cell edited row:" + e.getFirstRow()+" col:"+e.getColumn()+" is now ");
					//System.out.println(tblModel.getValueAt(e.getFirstRow(), e.getColumn()));
					model.getTarget().setWeight(e.getFirstRow(), tblModel.getValueAt(e.getFirstRow(), e.getColumn()));
					model.getBuild().heuristic(model.getTarget());
					System.out.println("This build's score is "+model.getBuild().getScore());
				}
			}
		});
	}
}
