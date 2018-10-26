package csvreader;

import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class WriteRoomModel {
	TreeMap<String, ArrayList<Observation>> map = new TreeMap<String, ArrayList<Observation>>();
	String url = "jdbc:mysql://localhost:8889/roommodel";
	String user = "root";
	String pass = "root";
	String SQL;

	public WriteRoomModel(String roomName, Observation o) {

		if (roomName.equals("RoomA")) {
			SQL = "INSERT INTO  `room1`(`date`, `roomid`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
		} else if (roomName.equals("RoomB")) {
			SQL = "INSERT INTO  `room2`(`date`, `roomid`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
		} else if (roomName.equals("RoomC")) {
			SQL = "INSERT INTO  `room3`(`date`, `roomid`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
		} else if (roomName.equals("RoomD")) {
			SQL = "INSERT INTO  `room4`(`date`, `roomid`, `bleobservation`, `wifiobservation`)  VALUES (?,?,?,?)";
		} else {
			return;
		}

		try (Connection connection = DriverManager.getConnection(url, user, pass);
				PreparedStatement statement = connection.prepareStatement(SQL);) {
			connection.setAutoCommit(false);

			statement.setString(1, o.datetime);
			statement.setString(2, roomName);
			statement.setString(3, o.bleObservation);
			statement.setString(4, o.wifiObservation);
			statement.addBatch();
			System.out.println(statement.toString());

			// System.out.println("erro");
			// 以下確認用プログラム

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
			System.out.println("Oh");
		}
	}
}
