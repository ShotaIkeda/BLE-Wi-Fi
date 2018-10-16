package csvreader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class ReadObservation {
	TreeMap<String, ArrayList<Observation>> map = new TreeMap<String, ArrayList<Observation>>();
	String url = "jdbc:mysql://localhost:8889/maintenance";
	String user = "root";
	String pass = "root";
	String SQL = "select * from observationData;";
	
	public ReadObservation(int boundary) {
		try (Connection con = DriverManager.getConnection(url, user, pass);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQL);) {
			while (rs.next()) {
				System.out.println(rs.getString("bleobservation"));
				Observation o = new Observation();
			    // olist.add(o);
			    o.setBLEObservation(rs.getString("bleobservation"));
			    o.setWifiObservation(rs.getString("wifiobservation"));
			    // 分割した文字を画面出力する
			    String datetime = rs.getString("date");
			    o.setDateTime(datetime);

			    String roomid = "???";
			      o.setRoomId(roomid);
			    ArrayList<Observation> olist = map.get(roomid);
			    if (olist == null) {
			      olist = new ArrayList<Observation>();
			      map.put(roomid, olist);
			    }
			    olist.add(o);
			    
			    String line_ble = rs.getString("bleobservation");
			    StringTokenizer token;
			    //System.out.println(line);
			    // 区切り文字","で分割する
			    token = new StringTokenizer(line_ble, "|");
			    while (token.hasMoreTokens()) {
			      String ble_token = token.nextToken();
			      StringTokenizer token2;
			      token2 = new StringTokenizer(ble_token, ",");
			      while (token2.hasMoreTokens()) {
			        // BLEの時
			        String uuid = token2.nextToken();
			        int major = Integer.parseInt(token2.nextToken());
			        int minor = Integer.parseInt(token2.nextToken());
			        int rssi = Integer.parseInt(token2.nextToken());
			        if(rssi>-100){
			        	BLEObservation bo = new BLEObservation(uuid, major, minor, rssi);
			        	o.addBLEObservation(bo);
			        }
			        //bo.disp();
			      }
			      //o.disp();
			    }
			    String line_wifi = rs.getString("wifiobservation");	
			    token = new StringTokenizer(line_wifi,"|");
			    while(token.hasMoreTokens()){
			        // WiFiの時
			      String wifi_token = token.nextToken();
			      //System.out.println(wifi_token);
			      StringTokenizer token3;
			      token3 = new StringTokenizer(wifi_token, ",");
			      while (token3.hasMoreTokens()) {
			    	  String ssid = token3.nextToken();
				    	String bssid;
				    	if(ssid.indexOf(":")==-1){
				    		bssid = token3.nextToken();
				    	}else{
				    		bssid = ssid;
				    		ssid = "";
				    	}
				        int rssi = Integer.parseInt(token3.nextToken());
				        if(rssi >= boundary){
				        	WifiObservation wo = new WifiObservation(ssid, bssid, rssi);
				        	//wo.disp();
				        	o.addWifiObservation(wo);
				        }
			      }
			    }
			    //o.disp();
			}
		} catch (Exception e) {
			e.toString();
		}
	}
	public TreeMap<String,ArrayList<Observation>> getMapData(){
		return map;
	}
}
