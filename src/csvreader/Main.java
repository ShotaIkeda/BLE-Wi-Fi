package csvreader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class Main extends JFrame implements ActionListener {

	String[][] data = { { "RoomA", "異常なし", "異常なし" }, 
						{ "RoomB", "異常なし", "異常なし" },
						{ "RoomC", "異常なし", "異常なし" },
						{ "RoomD", "異常なし", "異常なし" }};
	
	String[] columns = { "部屋名", "<html>観測できないはずなのに観測できる<br>&emsp;&emsp;&emsp;&emsp;(ビーコンの移動)<html>", "<html>観測できるはずなのに観測できない<br>&emsp;&emsp;(電池切れ・故障・持ち出し)" };
	
	DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
	
	JButton buttonA = new JButton("RoomA");
	JButton buttonB = new JButton("RoomB");
	JButton buttonC = new JButton("RoomC");
	JButton buttonD = new JButton("RoomD");
	
	public static void main(String args[]) throws Exception {
		Main frame = new Main("BLE Defects Detection");
		frame.setVisible(true);
	}
	  
	public Main(String title) {
		setLayout(new BorderLayout());
		//setSize(900, 700);
		setBounds(180, 100, 1100, 700);
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTable table = new JTable(tableModel);
		
	    for(int i = 0 ; i < 4 ; i++){
	        tableModel.addRow(data[i]);
	    }
		
		table.setFont(new Font(table.getFont().getFamily(), Font.PLAIN, 18));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(80);
		table.setGridColor(Color.BLACK);
		JTableHeader jh = table.getTableHeader();
		jh.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		jh.setBorder(new LineBorder(Color.BLACK));
		
		//JButton buttonA = new JButton("RoomA");
		buttonA.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		buttonA.addActionListener(this);
		//JButton buttonB = new JButton("RoomB");
		buttonB.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		buttonB.addActionListener(this);
		//JButton buttonC = new JButton("RoomC");
		buttonC.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		buttonC.addActionListener(this);
		//JButton buttonD = new JButton("RoomD");
		buttonD.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		buttonD.addActionListener(this);

		JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
	    p.add(buttonA);
	    p.add(buttonB);
	    p.add(buttonC);
	    p.add(buttonD);
		
		
		DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
		tableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);
		table.getColumnModel().getColumn(1).setCellRenderer(tableCellRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(tableCellRenderer);
		jh.getColumnModel().getColumn(0).setHeaderRenderer(tableCellRenderer);
		jh.getColumnModel().getColumn(1).setHeaderRenderer(tableCellRenderer);
		jh.getColumnModel().getColumn(2).setHeaderRenderer(tableCellRenderer);

		add(jh, BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);
		add(p, BorderLayout.SOUTH);
		
        Timer timer = new Timer();
        TimerTask timertask = new MyTimerTask(this);
        timer.scheduleAtFixedRate(timertask, 1000, 10000);
	}

	
	public void Update() {
		// TODO Auto-generated method stub
		try {
			TreeMap<String,ArrayList<String>> moveHereDispMap;
			TreeMap<String,ArrayList<String>> moveToSomewhereDispMap;
			TreeMap<String,String> errorMoveHereDateMap;
			TreeMap<String,String> errorMoveToSomewhereDateMap;
			Operation o = new Operation();
			moveHereDispMap = o.getCompMoveHereMap();
			moveToSomewhereDispMap = o.getCompMoveToSomewhereMap();
			errorMoveHereDateMap = o.getErrorMoveHereDate();
			errorMoveToSomewhereDateMap = o.getErrorMoveToSomewhereDate();

			for(int i=0;i<data.length;i++){
				tableModel.setValueAt("異常なし", i, 1);
				tableModel.setValueAt("異常なし", i, 2);
			}
			
			Iterator<String> it1 = moveHereDispMap.keySet().iterator();
	        while (it1.hasNext()) {
	            String key = it1.next();
	            for(int i=0;i<data.length;i++){	
	            	String defect_label1 = "異常なし";
	            	if(key.equals(data[i][0])){
	            		ArrayList<String> moveHereDispList = moveHereDispMap.get(key);
	            		for(int j=0;j<moveHereDispList.size();j++){
	            			if(j==0){
	            				defect_label1 = "<html><font color="+"RED"+">"+errorMoveHereDateMap.get(moveHereDispList.get(j))+"　ビーコン:"+moveHereDispList.get(j).substring(37,41)+"</font><html>";
	            			}else{
	            				defect_label1 = "<html><font color="+"RED"+">"+defect_label1+"<br>"+ errorMoveHereDateMap.get(moveHereDispList.get(j))+"　ビーコン:"+moveHereDispList.get(j).substring(37,41)+"<html>";
	            			}
	            			tableModel.setValueAt(defect_label1, i, 1);
	            		}
	            	}
				}
	        }
	        Iterator<String> it2 = moveToSomewhereDispMap.keySet().iterator();
	        while (it2.hasNext()) {
	            String key = it2.next();
	            for(int i=0;i<data.length;i++){	
	            	String defect_label2 = "異常なし";
	            	if(key.equals(data[i][0])){
	            		ArrayList<String> moveToSomewhereDispList = moveToSomewhereDispMap.get(key);
	            		for(int j=0;j<moveToSomewhereDispList.size();j++){
	            			if(j==0){
	            				defect_label2 = "<html><font color="+"RED"+">"+errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))+"　ビーコン:"+moveToSomewhereDispList.get(j).substring(37,41)+"</font><html>";
	            			}else{
	            				defect_label2 = "<html>"+defect_label2+"<br>"+ "<font color="+"RED"+">"+errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))+"　ビーコン:"+moveToSomewhereDispList.get(j).substring(37,41)+"</font><html>";
	            			}
	            		}
	            		tableModel.setValueAt(defect_label2, i, 2);
	            	}
				}
	        }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == buttonA) {
			WriteMentenanceDate wrd = new WriteMentenanceDate("RoomA");
		}else if(e.getSource() == buttonB){
			WriteMentenanceDate wrd = new WriteMentenanceDate("RoomB");
		}else if(e.getSource() == buttonC){
			WriteMentenanceDate wrd = new WriteMentenanceDate("RoomC");
		}else if(e.getSource() == buttonD){
			WriteMentenanceDate wrd = new WriteMentenanceDate("RoomD");
		}
	}
}
