package csvreader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class AcquiredData {
	TreeMap<String, ArrayList<Observation>> map = new TreeMap<String, ArrayList<Observation>>();

	public void AcquiredData() {
	}

	public void setAcquiredData() {
		// ArrayList<Observation> olist = new ArrayList<Observation>();

		try {
			// ファイルを読み込む
			//ReadObservation rd =new ReadObservation();
			//FileReader fr = new FileReader("src/csvreader/exper2.csv");
			//FileReader fr = new FileReader("src/csvreader/exper1.csv");
			FileReader fr = new FileReader("src/csvreader/exper3.csv");
			//FileReader fr = new FileReader("src/csvreader/exper_lost.csv");
			BufferedReader br = new BufferedReader(fr);
			br.readLine();
			// 読み込んだファイルを１行ずつ処理する
			String line;
			StringTokenizer token;
			while ((line = br.readLine()) != null) {
				if (line.indexOf(",") == -1) {
					continue;
				}
				//System.out.println(line);
				// 区切り文字","で分割する
				token = new StringTokenizer(line, ",");

				Observation o = new Observation();
				// olist.add(o);
				

				// 分割した文字を画面出力する
				String datetime = token.nextToken();
				o.setDateTime(datetime);

				String roomid = token.nextToken();
				o.setRoomId(roomid);
				ArrayList<Observation> olist = map.get(roomid);
				if (olist == null) {
					olist = new ArrayList<Observation>();
					map.put(roomid, olist);
				}
				olist.add(o);

				while (token.hasMoreTokens()) {
					String s1 = token.nextToken();
					String s2 = token.nextToken();
					if (s2.indexOf(":") == -1) {
						int num = Integer.parseInt(s2);
						// BLEの時
						String uuid = s1;
						int major = num;
						int minor = Integer.parseInt(token.nextToken());
						int rssi = Integer.parseInt(token.nextToken());
						BLEObservation bo = new BLEObservation(uuid, major, minor, rssi);
						o.addBLEObservation(bo);
						//bo.disp();
					} else {
						// WiFiの時
						String ssid = s1;
						String bssid = s2;
						int rssi = Integer.parseInt(token.nextToken());
						WifiObservation wo = new WifiObservation(ssid, bssid, rssi);
						o.addWifiObservation(wo);
					}
				}
			//o.disp();
				}
			// 終了処理
			br.close();
		} catch (IOException ex) {
			// 例外発生時処理
			ex.printStackTrace();
		}
	}

	public TreeMap<String, ArrayList<Observation>> getdata() {
		return map;
	}
}
