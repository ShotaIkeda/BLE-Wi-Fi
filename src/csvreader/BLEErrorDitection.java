package csvreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class BLEErrorDitection {
	//public static int boundary = -50;// 下限電波強度
	//public static int boundary = -70;// 下限電波強度
	public static int boundary = -75;// 下限電波強度
	//public static int boundary = -90;// 下限電波強度
	public static double PROB_MAX=0.99;

	public static void main(String[] args) throws Exception{
		File learningFile = new File("src/csvreader/allroomdata.csv");
		File roomDetectionResultFile = new File("src/csvreader/roomDetection1_75.csv");

		File testFile = new File("src/csvreader/exper1.csv");
		File errorMoveHereDetectionResultFile = new File("src/csvreader/errorMHDetection1.txt");
		File errorMoveToSomewhereDetectionResultFile = new File("src/csvreader/errorMTDetection1.txt");
		
		//File testFile = new File("src/csvreader/exper2.csv");
		//File errorMoveHereDetectionResultFile = new File("src/csvreader/errorMHDetection2.txt");
		//File errorMoveToSomewhereDetectionResultFile = new File("src/csvreader/errorMTDetection2.txt");

		
		
		// 学習用Observationの読み込み
		TreeMap<String, ArrayList<Observation>> learningObsMap = readObservation(learningFile);

		// BLE/WiFiモデルの入れ物を作っておく
		TreeMap<String, BLEModel> bleMap = new TreeMap<String, BLEModel>();
		TreeMap<String, WifiRoomModel> wifiMap = new TreeMap<String, WifiRoomModel>();

		// BLE/WiFiモデル作成
		generateModel(learningObsMap, bleMap, wifiMap);

		// 評価データ読み込み
		TreeMap<String, ArrayList<Observation>> testObsMap = readObservation(testFile);
		
		//部屋検出
		//detectRoom(testObsMap,bleMap,wifiMap,roomDetectionResultFile);

		// BLE異常検出(Move here)
		detectAllBLEMoveHere(testObsMap,bleMap,wifiMap,errorMoveHereDetectionResultFile);
		
		detectAllBLEMoveToSomewhere(testObsMap,bleMap,wifiMap,errorMoveToSomewhereDetectionResultFile);
	}
	
	public static void detectRoom(TreeMap<String, ArrayList<Observation>> obsMap,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,File file) throws Exception{
		
		PrintWriter out=new PrintWriter(file);
		out.println("CorrectRoom,BLERoom,WiFiRoom");
		
		Iterator<String> it=obsMap.keySet().iterator();
		while(it.hasNext()){
			String room=it.next();
			ArrayList<Observation> obslist=obsMap.get(room);
			
			System.out.println(room);
			for(Observation o:obslist){
				//BLEモデルに基づいて推定した部屋
				String rb=detectRoomByBLE(o.blelist, bleMap);
				
				//WiFiモデルに基づいて推定した部屋
				String rw=detectRoomByWifi(o.wifilist, wifiMap);
				
				System.out.println("Room detected by BLE:"+rb);
				System.out.println("Room detected by WiFi:"+rw);
				
				
				out.println(room+","+rb+","+rw);
				out.flush();
			}
		}
		
		out.close();
	}
	
	public static void detectAllBLEMoveToSomewhere(TreeMap<String, ArrayList<Observation>> obsMap,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,File file) throws Exception{
		PrintWriter out=new PrintWriter(file);

		//あるはずのものがない異常は複数の観測から発見するためここで初期化
		ArrayList<String> moveToSomewhereErrorList=new ArrayList<String>();

		int count=1;
		Iterator<String> it=obsMap.keySet().iterator();
		while(it.hasNext()){
			
			String room=it.next();
			ArrayList<Observation> obslist=obsMap.get(room);
			
			for(Observation o:obslist){
				log("----------------------------------------------",out);
				log("ID:"+count,out);
				
				//1回の観測で，あるはずのものがない異常の候補を探す
				detectBLEMoveToSomewhere(o,bleMap,wifiMap,moveToSomewhereErrorList,out);

				count++;
			}
		}
		log("+++++++++++++++++++++++++++++++++++++++++++++++++",out);
		log("Final Error Candidates:"+moveToSomewhereErrorList,out);
		
		
		out.close();
		
	}
	
	public static void detectAllBLEMoveHere(TreeMap<String, ArrayList<Observation>> obsMap,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,File file) throws Exception{
		PrintWriter out=new PrintWriter(file);

		int count=1;
		Iterator<String> it=obsMap.keySet().iterator();
		while(it.hasNext()){
			String room=it.next();
			ArrayList<Observation> obslist=obsMap.get(room);
			
			for(Observation o:obslist){
				log("----------------------------------------------",out);
				log("ID:"+count,out);
				
				//ないはずのものがある異常
				detectBLEMoveHere(o,bleMap,wifiMap,out);
				
				count++;
			}
		}
		out.close();
	}
	
	public static void detectBLEMoveHere(Observation o,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,PrintWriter out){
		
		//部屋推定で調査する部屋を決定
		//BLEモデルに基づいて推定した部屋
		String rb=detectRoomByBLE(o.blelist, bleMap);
		
		//WiFiモデルに基づいて推定した部屋
		String rw=detectRoomByWifi(o.wifilist, wifiMap);
		
		log("Correct:"+o.roomid+",BLE:"+rb+",WiFi:"+rw,out );

	
		//チェクするべき部屋を決定
		ArrayList<String> roomList=new ArrayList<String>();
		if(rb!=null && !"".equals(rb)){
			roomList.add(rb);	
		}
		if(rw!=null && !"".equals(rw) && !roomList.contains(rw)){
			roomList.add(rw);	
		}
		
		TreeMap<String,ArrayList<String>> errorMap=new TreeMap<String, ArrayList<String>>(); 
		
		ArrayList<String> bleKeyList=bleObservationListToBleKeyList(o.blelist);
		//log("観測されたBLE電波："+bleKeyList,out);


		//準備
		for(String room:roomList){
			errorMap.put(room, new ArrayList(bleKeyList));
		}

		
		for(String room:roomList){
			ArrayList<String> obsBleKeyList=errorMap.get(room);
			
			BLEModel bm=bleMap.get(room);
			
			//observationにあってblemodelにないものを探す
			for(int i=obsBleKeyList.size()-1;i>=0;i--){
				String blekey=obsBleKeyList.get(i);

				//見つけたら消していく．最後まで残ったものは，ないはずのもの．
				if(bm.map.containsKey(blekey)){
					obsBleKeyList.remove(blekey);
				}
			}
		}
		
		
		for(String room:roomList){
			ArrayList<String> list=errorMap.get(room);
			if(list.size()==0)continue;
			log("別部屋から"+room+"に移動してきてた可能性のあるBLEビーコン:"+list,out);
		}
	}
	
	public static void log(String mes,PrintWriter out){
		System.out.println(mes);
		out.println(mes);
		out.flush();
	}
	
	public static ArrayList<String> bleObservationListToBleKeyList(ArrayList<BLEObservation> blelist){
		ArrayList<String> list=new ArrayList<String>();
		
		for(BLEObservation o:blelist){
			String key=BLEModel.generateKey(o.uuid, o.major, o.minor);
			list.add(key);
		}
		return list;
	}

	
	//
	public static void detectBLEMoveToSomewhere(Observation o,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,ArrayList<String> errorList,PrintWriter out){
		
		//部屋推定で調査する部屋を決定
		//BLEモデルに基づいて推定した部屋
		String rb=detectRoomByBLE(o.blelist, bleMap);
		
		//WiFiモデルに基づいて推定した部屋
		String rw=detectRoomByWifi(o.wifilist, wifiMap);

		
		log("Correct:"+o.roomid+",BLE:"+rb+",WiFi:"+rw,out );

		//チェクするべき部屋を決定
		ArrayList<String> roomList=new ArrayList<String>();
		if(rb!=null && !"".equals(rb)){
			roomList.add(rb);	
		}
		if(rw!=null && !"".equals(rw) && !roomList.contains(rw)){
			roomList.add(rw);	
		}
		
		TreeMap<String,ArrayList<String>> errorMap=new TreeMap<String, ArrayList<String>>(); 
		
		ArrayList<String> bleKeyList=bleObservationListToBleKeyList(o.blelist);
		//log("観測されたBLE電波："+bleKeyList,out);
		
		
		
		
		//準備．あるはずなのにないものを見つけるために，各部屋でみえるはずのBLEビーコンリストを用意する．
		for(String room:roomList){
			BLEModel bm=bleMap.get(room);
			
			errorMap.put(room, new ArrayList(bm.map.keySet()));
		}
		
		//準備．1回の観測において正常動作していると確認できたビーコンリストを得る
		TreeMap<String,ArrayList<String>> correctMap=new TreeMap<String, ArrayList<String>>(); 
		for(String room:roomList){
			BLEModel bm=bleMap.get(room);
			correctMap.put(room, new ArrayList(bm.map.keySet()));
		}

		
		for(String room:roomList){
			ArrayList<String> blelist=errorMap.get(room);
			for(String blekey:bleKeyList){
				//みえたビーコンを消していく
				if(blelist.contains(blekey)){
					blelist.remove(blekey);
				}
			}
		}
		
		for(String room:roomList){
			ArrayList<String> list=errorMap.get(room);
			if(list.size()==0)continue;
			log(room+"からなくなってしまった可能性のあるBLEビーコン:"+list,out);

			//移動してしまった可能性のあるビーコンをリストに蓄積
			for(String blekey:list){
				if(!errorList.contains(blekey)){
					errorList.add(blekey);
				}
			}
			
			ArrayList<String> correctList=correctMap.get(room);
			for(String blekey:list){
				correctList.remove(blekey);
			}
		}
		
		//正常動作しているものは異常候補から外しておく
		
		for(String room:roomList){
			ArrayList<String> correctList=correctMap.get(room);
			
			for(int i=errorList.size()-1;i>=0;i--){
				String blekey=errorList.get(i);
				if(correctList.contains(blekey)){
					errorList.remove(blekey);
				}
			}
		}
		
	}

	
	public static BLEModel generateBLEModel(ArrayList<Observation> olist) {
		BLEModel bm = new BLEModel();
		TreeMap<String, Integer> countmap = new TreeMap<String, Integer>();
		for (Observation o : olist) {
			ArrayList<BLEObservation> bolist = o.blelist;
			for (BLEObservation bo : bolist) {
				String key = BLEModel.generateKey(bo.uuid, bo.major, bo.minor);
				if (countmap.containsKey(key)) {
					int count = countmap.get(key);
					count++;
					countmap.put(key, count);
				} else {
					countmap.put(key, 1);
				}
			}
		}

		Iterator<String> it = countmap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			int count = countmap.get(key);
			// 確率計算
			// System.out.println(key+" "+count);
			double probability = (double) count / (double) olist.size();
			if(probability>PROB_MAX){
				probability=PROB_MAX;
			}
			bm.setABLEModel(key, probability);
		}
		return bm;
	}

	public static WifiRoomModel generateWifiModel(ArrayList<Observation> olist) {
		WifiRoomModel wm = new WifiRoomModel();

		TreeMap<String, Integer> countmap = new TreeMap<String, Integer>();
		for (Observation o : olist) {
			ArrayList<WifiObservation> wolist = o.wifilist;
			for (WifiObservation wo : wolist) {
				String key = WifiRoomModel.generateKey(wo.bssid);
				if (countmap.containsKey(key)) {
					int count = countmap.get(key);
					count++;
					countmap.put(key, count);
				} else {
					countmap.put(key, 1);
				}
			}
		}

		Iterator<String> it = countmap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			int count = countmap.get(key);
			if (count > 10) {
				double probability = (double) count / (double) olist.size();
				// System.out.println(key+" = "+count+":"+probability*100+"%");
				if(probability>PROB_MAX){
					probability=PROB_MAX;
				}
				wm.setAWifiModel(key, probability);
			}
		}
		return wm;
	}

	// ObservationからBLE/WiFiモデルを作成．
	public static void generateModel(TreeMap<String, ArrayList<Observation>> obsMap, TreeMap<String, BLEModel> bleMap,
			TreeMap<String, WifiRoomModel> wifiMap) {
		Iterator<String> it = obsMap.keySet().iterator();
		while (it.hasNext()) {
			String roomid = it.next();
			ArrayList<Observation> olist = obsMap.get(roomid);

			BLEModel bm = generateBLEModel(olist);
			WifiRoomModel wm = generateWifiModel(olist);

			bleMap.put(roomid, bm);
			wifiMap.put(roomid, wm);

		}
	}

	public static TreeMap<String, ArrayList<Observation>> readObservation(File file) {
		TreeMap<String, ArrayList<Observation>> obsMap = new TreeMap<String, ArrayList<Observation>>();
		try {
			// ファイルを読み込む
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();
			// 読み込んだファイルを１行ずつ処理する
			String line;
			StringTokenizer token;
			while ((line = br.readLine()) != null) {
				if (line.indexOf(",") == -1) {
					continue;
				}

				// 区切り文字","で分割する
				token = new StringTokenizer(line, ",");

				Observation o = new Observation();
				// olist.add(o);

				// 分割した文字を画面出力する
				String datetime = token.nextToken();
				o.setDateTime(datetime);

				String roomid = token.nextToken();
				o.setRoomId(roomid);
				ArrayList<Observation> olist = obsMap.get(roomid);
				if (olist == null) {
					olist = new ArrayList<Observation>();
					obsMap.put(roomid, olist);
				}
				olist.add(o);

				while (token.hasMoreTokens()) {
					String s1 = token.nextToken();
					String s2 = token.nextToken();
					// System.out.println(s1+","+s2);
					if (s2.indexOf(":") == -1) {
						int num = Integer.parseInt(s2);
						// BLEの時
						String uuid = s1;
						int major = num;
						int minor = Integer.parseInt(token.nextToken());
						int rssi = Integer.parseInt(token.nextToken());
						BLEObservation bo = new BLEObservation(uuid, major, minor, rssi);
						o.addBLEObservation(bo);
					} else {
						// WiFiの時
						String ssid = s1;
						String bssid = s2;
						int rssi = Integer.parseInt(token.nextToken());
						if (rssi >= boundary) {
							WifiObservation wo = new WifiObservation(ssid, bssid, rssi);
							o.addWifiObservation(wo);
						}
					}
				}
			}
			// 終了処理
			br.close();
		} catch (IOException ex) {
			// 例外発生時処理
			ex.printStackTrace();
		}
		return obsMap;
	}
	

	public static String detectRoomByBLE(ArrayList<BLEObservation> blelist, TreeMap<String, BLEModel> blemap) {
		double maxprob = 0;
		String maxRoomid = "";
		Iterator<String> it = blemap.keySet().iterator();
		while (it.hasNext()) {
			String roomid = it.next();
			BLEModel bm = blemap.get(roomid);
			double probability = calcProbability(blelist, bm, roomid);
			if (maxprob < probability) {
				maxRoomid = roomid;
				maxprob = probability;
			}
		}
		return maxRoomid;
	}

	public static String detectRoomByWifi(ArrayList<WifiObservation> wifilist, TreeMap<String, WifiRoomModel> wifimap) {
		double maxprob = 0;
		String maxRoomid = "";
		Iterator<String> it = wifimap.keySet().iterator();
		while (it.hasNext()) {
			String roomid = it.next();
			WifiRoomModel wm = wifimap.get(roomid);
			double probability = calcProbability(wifilist, wm, roomid);
			if (maxprob < probability) {
				maxRoomid = roomid;
				maxprob = probability;
			}
		}

		return maxRoomid;
	}

	public static double calcProbability(ArrayList<BLEObservation> blelist, BLEModel bm, String roomid) {
		Iterator<String> it = bm.map.keySet().iterator();
		ArrayList<String> listkey = new ArrayList<String>();
		double probability = 1;

		for (int i = 0; i < blelist.size(); i++) {
			listkey.add(blelist.get(i).uuid + "_" + blelist.get(i).major + "_" + blelist.get(i).minor);
		}

		while (it.hasNext()) {
			String key = it.next();
			if (listkey.indexOf(key) != -1) {
				probability = probability * bm.map.get(key);
			} else {
				probability = probability * (1 - bm.map.get(key));
			}
		}
		// System.out.println(roomid+":BLEprobability:"+probability*100+"%");
		return probability;
	}

	public static double calcProbability(ArrayList<WifiObservation> wifilist, WifiRoomModel wm, String roomid) {
		Iterator<String> it = wm.map.keySet().iterator();
		ArrayList<String> listkey = new ArrayList<String>();
		double probability = 1;

		for (int i = 0; i < wifilist.size(); i++) {
			listkey.add(wifilist.get(i).bssid);
		}

		while (it.hasNext()) {
			String key = it.next();
			if (listkey.indexOf(key) != -1) {
				probability = probability * wm.map.get(key);
			} else {
				probability = probability * (1 - wm.map.get(key));
			}
		}
		// System.out.println(roomid+":Wifiprobability:"+probability*100+"%");
		return probability;
	}
}
