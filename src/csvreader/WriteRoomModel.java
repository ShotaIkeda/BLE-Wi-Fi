package csvreader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class WriteRoomModel {
	TreeMap<String, ArrayList<Observation>> map = new TreeMap<String, ArrayList<Observation>>();
	String url = "jdbc:mysql://localhost:8889/roommodel";
	String user = "root";
	String pass = "root";
	String SQL;
	
	public WriteRoomModel(TreeMap<String, ArrayList<Observation>> admap) {
		String roomName = "";
		map = admap;
		Iterator<String> ite=map.keySet().iterator();
		while(ite.hasNext()){
			String room=ite.next();
			
			if(room == "RoomA"){
				SQL = "INSERT INTO  `room1`(`date`, `roomname`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
			}else if(room == "RoomB"){
				SQL = "INSERT INTO  `room2`(`roomname`, `date`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
			}else if(room == "RoomC"){
				SQL = "INSERT INTO  `room3`(`roomname`, `date`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
			}else if(room == "RoomD"){	
				SQL = "INSERT INTO  `room4`(`roomname`, `date`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
			}else{
				return;
			}
			try (Connection connection = DriverManager.getConnection(url, user, pass);
					PreparedStatement statement = connection.prepareStatement(SQL);) {
					connection.setAutoCommit(false);
		
					for(int i=0;i<map.get(room).size();i++){
						Observation o=map.get(room).get(i);
						statement.setString(1, room);
						statement.setString(2, o.datetime);		
						statement.setString(3, o.bleObservation);
						statement.setString(4, o.wifiObservation);
						statement.addBatch();	
						System.out.println(statement.toString());	
					}
					//以下確認用プログラム
					int[] result = statement.executeBatch();	
					System.out.println("登録：" + result.length + "件");
					try {
						connection.commit();
						System.out.println("登録成功");
					} catch (SQLException e) {
						connection.rollback();
						System.out.println("登録失敗：ロールバック実行");
						e.printStackTrace();	
					}
					//
			} catch (Exception e) {
			}
		}
	}
}
