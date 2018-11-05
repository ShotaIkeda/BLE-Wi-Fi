package csvreader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Timer;
import java.io.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class Main extends JFrame implements ActionListener {

	String[][] data = { { "RoomA", "異常なし", "異常なし" }, { "RoomB", "異常なし", "異常なし" }, { "RoomC", "異常なし", "異常なし" },
			{ "RoomD", "異常なし", "異常なし" } };

	String[] columns = { "部屋名", "<html>観測できないはずなのに観測できる<br>&emsp;&emsp;&emsp;&emsp;(ビーコンの移動)<html>",
			"<html>観測できるはずなのに観測できない<br>&emsp;&emsp;(電池切れ・故障・持ち出し)" };

	DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
	
	JButton buttonMente = new JButton("メンテナンス");

	int interval = 10000;
	
	int dateInterval = 2;
	Operation o = new Operation(dateInterval);

	SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	Date date_moveHere;
	Date date_moveToSomewhere;

	JPanel p = new JPanel();

	public static void main(String args[]) throws Exception {
		Main frame = new Main("BLE Defects Detection");
		frame.setVisible(true);
	}

	public Main(String title) {
		setLayout(new BorderLayout());
		// setSize(900, 700);
		setBounds(180, 100, 1100, 700);
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTable table = new JTable(tableModel);

		for (int i = 0; i < 4; i++) {
			tableModel.addRow(data[i]);
		}

		table.setFont(new Font(table.getFont().getFamily(), Font.PLAIN, 18));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(80);
		table.setGridColor(Color.BLACK);
		JTableHeader jh = table.getTableHeader();
		jh.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		jh.setBorder(new LineBorder(Color.BLACK));

		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));

		buttonMente.setFont(new Font(jh.getFont().getFamily(), Font.PLAIN, 20));
		buttonMente.addActionListener(this);
		p.add(buttonMente);

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
		timer.scheduleAtFixedRate(timertask, 0, interval);
	}

	public void Update() {
		// TODO Auto-generated method stub
		try {
			TreeMap<String, ArrayList<String>> moveHereDispMap;
			TreeMap<String, ArrayList<String>> moveToSomewhereDispMap;
			TreeMap<String, String> errorMoveHereDateMap;
			TreeMap<String, String> errorMoveToSomewhereDateMap;
			o.RunOperation();
			moveHereDispMap = o.getCompMoveHereMap();
			moveToSomewhereDispMap = o.getCompMoveToSomewhereMap();
			errorMoveHereDateMap = o.getErrorMoveHereDate();
			errorMoveToSomewhereDateMap = o.getErrorMoveToSomewhereDate();

			for (int i = 0; i < data.length; i++) {
				tableModel.setValueAt("異常なし", i, 1);
				tableModel.setValueAt("異常なし", i, 2);
			}
			try {
				Iterator<String> it1 = moveHereDispMap.keySet().iterator();
				while (it1.hasNext()) {
					String key = it1.next();
					for (int i = 0; i < data.length; i++) {
						String defect_label1 = "異常なし";
						if (key.equals(data[i][0])) {
							ArrayList<String> moveHereDispList = moveHereDispMap.get(key);
							for (int j = 0; j < moveHereDispList.size(); j++) {
								Date date = fmt.parse(errorMoveHereDateMap.get(moveHereDispList.get(j)));
								Date compDate = fmt.parse(fmt.format(new Date()));

								long dateTimeTo = date.getTime();
								long dateTimeFrom = compDate.getTime();
								long dayDiff = (dateTimeFrom - dateTimeTo) / (1000 * 60 * 60 * 24);

								if (dayDiff > dateInterval) {
									if (j == 0) {
										defect_label1 = "<html><font color=" + "RED" + ">"
												+ errorMoveHereDateMap.get(moveHereDispList.get(j)) + "　ビーコン:"
												+ moveHereDispList.get(j).substring(37, 41) + "</font><html>";
									} else {
										defect_label1 = "<html><font color=" + "RED" + ">" + defect_label1 + "<br>"
												+ errorMoveHereDateMap.get(moveHereDispList.get(j)) + "　ビーコン:"
												+ moveHereDispList.get(j).substring(37, 41) + "<html>";
									}
								} else {
									if (j == 0) {
										defect_label1 = "<html><font color=" + "BLUE" + ">"
												+ errorMoveHereDateMap.get(moveHereDispList.get(j)) + "　ビーコン:"
												+ moveHereDispList.get(j).substring(37, 41) + "</font><html>";
									} else {
										defect_label1 = "<html><font color=" + "BLUE" + ">" + defect_label1 + "<br>"
												+ errorMoveHereDateMap.get(moveHereDispList.get(j)) + "　ビーコン:"
												+ moveHereDispList.get(j).substring(37, 41) + "<html>";
									}
								}
							}
							tableModel.setValueAt(defect_label1, i, 1);
						}
					}
				}
			} catch (NullPointerException e) {

			}

			try {
				Iterator<String> it2 = moveToSomewhereDispMap.keySet().iterator();
				while (it2.hasNext()) {
					String key = it2.next();
					for (int i = 0; i < data.length; i++) {
						String defect_label2 = "異常なし";
						if (key.equals(data[i][0])) {
							ArrayList<String> moveToSomewhereDispList = moveToSomewhereDispMap.get(key);
							for (int j = 0; j < moveToSomewhereDispList.size(); j++) {
								Date date = fmt.parse(errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j)));
								Date compDate = fmt.parse(fmt.format(new Date()));

								long dateTimeTo = date.getTime();
								long dateTimeFrom = compDate.getTime();
								long dayDiff = (dateTimeFrom - dateTimeTo) / (1000 * 60 * 60 * 24);

								if (dayDiff > dateInterval) {

									if (j == 0) {
										defect_label2 = "<html><font color=" + "RED" + ">"
												+ errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))
												+ "　ビーコン:" + moveToSomewhereDispList.get(j).substring(37, 41)
												+ "</font><html>";
									} else {
										defect_label2 = "<html>" + defect_label2 + "<br>" + "<font color=" + "RED" + ">"
												+ errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))
												+ "　ビーコン:" + moveToSomewhereDispList.get(j).substring(37, 41)
												+ "</font><html>";
									}
								} else {

									if (j == 0) {
										defect_label2 = "<html><font color=" + "BLUE" + ">"
												+ errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))
												+ "　ビーコン:" + moveToSomewhereDispList.get(j).substring(37, 41)
												+ "</font><html>";
									} else {
										defect_label2 = "<html>" + defect_label2 + "<br>" + "<font color=" + "BLUE"
												+ ">" + errorMoveToSomewhereDateMap.get(moveToSomewhereDispList.get(j))
												+ "　ビーコン:" + moveToSomewhereDispList.get(j).substring(37, 41)
												+ "</font><html>";
									}
								}
							}
							tableModel.setValueAt(defect_label2, i, 2);
						}
					}
				}
			} catch (NullPointerException e) {

			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Update();
		
		String selectAnswer[] = {"いいえ","はい"};
		String selectValues[] = { "RoomD", "RoomC", "RoomB", "RoomA" };
		int selectNumber = -1;

		if (e.getSource() == buttonMente) {
			
			int answerChose = JOptionPane.showOptionDialog(this, "不具合は発生していないですか？", "不具合の確認", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, selectAnswer, selectAnswer[0]);
			if (answerChose != JOptionPane.CLOSED_OPTION && selectAnswer[answerChose].equals("はい")) {
				
				int select = JOptionPane.showOptionDialog(this, "どの部屋のメンテナンスをしましたか？", "部屋メンテナンス", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, selectValues, selectValues[0]);
				if (select != JOptionPane.CLOSED_OPTION) {
					selectNumber = select;
				}
			}
		}

		if (selectNumber != -1) {
			TreeMap<String, ArrayList<String>> moveHereMap = o.getCompMoveHereMap();
			TreeMap<String, ArrayList<String>> moveToSomewhereMap = o.getCompMoveToSomewhereMap();
			
			JLabel label;
			
			if (selectValues[selectNumber].equals("RoomA")) {
				if(moveHereMap.get("RoomA") == null && moveToSomewhereMap.get("RoomA") == null){
					WriteMentenanceDate wrd = new WriteMentenanceDate("RoomA");
					label = new JLabel("メンテナンス終了");
					JOptionPane.showMessageDialog(this, label);
				}else{
					label = new JLabel("不具合があります");
					JOptionPane.showMessageDialog(this, label);
				}
			} else if (selectValues[selectNumber].equals("RoomB")) {
				if(moveHereMap.get("RoomB") == null && moveToSomewhereMap.get("RoomB") == null){
					WriteMentenanceDate wrd = new WriteMentenanceDate("RoomB");
					label = new JLabel("メンテナンス終了");
					JOptionPane.showMessageDialog(this, label);
				}else{
					label = new JLabel("不具合があります");
					JOptionPane.showMessageDialog(this, label);
				}
			} else if (selectValues[selectNumber].equals("RoomC")) {
				if(moveHereMap.get("RoomC") == null && moveToSomewhereMap.get("RoomC") == null){
					WriteMentenanceDate wrd = new WriteMentenanceDate("RoomC");
					label = new JLabel("メンテナンス終了");
					JOptionPane.showMessageDialog(this, label);
				}else{
					label = new JLabel("不具合があります");
					JOptionPane.showMessageDialog(this, label);
				}
			} else if (selectValues[selectNumber].equals("RoomD")) {
				if(moveHereMap.get("RoomD") == null && moveToSomewhereMap.get("RoomD") == null){
					WriteMentenanceDate wrd = new WriteMentenanceDate("RoomD");
					label = new JLabel("メンテナンス終了");
					JOptionPane.showMessageDialog(this, label);
				}else{
					label = new JLabel("不具合があります");
					JOptionPane.showMessageDialog(this, label);
				}
			}
		}
	}
}
