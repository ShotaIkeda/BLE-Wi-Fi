package csvreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.io.*;
import java.text.SimpleDateFormat;

public class Operation {
	static TreeMap<String, ArrayList<String>> compMoveHereMap;
	static TreeMap<String, ArrayList<String>> compMoveToSomewhereMap;
	static TreeMap<String, String> errorMoveHereDateMap;
	static TreeMap<String, String> errorMoveToSomewhereDateMap;
	
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date;
	static int dateInterval;
	
	public Operation(int dateInterval){
		this.dateInterval = dateInterval;
	}
	
	public void RunOperation() throws Exception {
		TreeMap<String, ArrayList<Observation>> map = new TreeMap<String, ArrayList<Observation>>();
		TreeMap<String, ArrayList<Observation>> admap = new TreeMap<String, ArrayList<Observation>>();
		TreeMap<String, BLEModel> blemap = new TreeMap<String, BLEModel>();
		TreeMap<String, WifiRoomModel> wifimap = new TreeMap<String, WifiRoomModel>();

		int boundary = -75;

		ReadRoomModel rm = new ReadRoomModel(boundary);
		map = rm.getMapData();

		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String roomid = it.next();
			ArrayList<Observation> olist = map.get(roomid);

			BLEModel bm = generateBLEModel(olist);
			WifiRoomModel wm = generateWifiModel(olist);

			blemap.put(roomid, bm);
			wifimap.put(roomid, wm);
		}

		ReadObservation ro = new ReadObservation(boundary);
		admap = ro.getMapData();

		Iterator<String> ite = admap.keySet().iterator();
		while (ite.hasNext()) {
			String room = ite.next();
			for (int i = 0; i < admap.get(room).size(); i++) {
				Observation o = admap.get(room).get(i);
				
				Date newDate = fmt.parse(o.datetime);
				
				if(date == null){
					date = newDate;
				}else{
					if(date.before(newDate)){
						date = newDate;
					}else{
						continue;
					}
				}
				
				// dono heya?
				String bleroomid = detectRoomByBLE(o.blelist, blemap);
				String wifiroomid = detectRoomByWifi(o.wifilist, wifimap);
				//
				
				System.out.println(bleroomid + "," + wifiroomid);

				TreeMap<String, ArrayList<Observation>> admap2 = new TreeMap<>();
				ArrayList<Observation> obserList = new ArrayList<>();
				obserList.add(o);
				admap2.put(room, obserList);

				// 観測データ1行ごとに不具合推定
				ErrorDitection2 ed2 = new ErrorDitection2();
				ed2.setData(blemap, wifimap, admap2, boundary);
				ed2.Ditection();
				ed2.disp();
				//print(blemap,wifimap);

				// UIに表示
				compMoveHereMap = ed2.getMoveHereMap();
				compMoveToSomewhereMap = ed2.getMoveToSomewhere();
				errorMoveHereDateMap = ed2.getErrorMoveHereDate();
				errorMoveToSomewhereDateMap = ed2.getErrorMoveToSomewhereDate();
				
				/*if (compMoveToSomewhereMap.isEmpty() && errorMoveToSomewhereDateMap.isEmpty()) {
					System.out.println("エラー無し");
					//WriteRoomModel wrm = new WriteRoomModel(bleroomid, o);
				} else {*/
					// 自動更新プロセス
					AutoUpdateModel aum = new AutoUpdateModel(dateInterval);
					aum.setErrorData(compMoveHereMap, compMoveToSomewhereMap);
					aum.selectProcess(bleroomid,wifiroomid, o);

				//}
			}
		}
	}

	public static BLEModel generateBLEModel(ArrayList<Observation> olist) {
		BLEModel bm = new BLEModel();
		TreeMap<String, Integer> countmap = new TreeMap<String, Integer>();
		TreeMap<String, ArrayList<Integer>> rssimap = new TreeMap<String, ArrayList<Integer>>();
		for (Observation o : olist) {
			ArrayList<BLEObservation> bolist = o.blelist;
			for (BLEObservation bo : bolist) {
				String key = BLEModel.generateKey(bo.uuid, bo.major, bo.minor);
				if (countmap.containsKey(key)) {
					int count = countmap.get(key);
					ArrayList<Integer> rssiNum = rssimap.get(key);
					count++;
					rssiNum.add(bo.rssi);
					countmap.put(key, count);
					rssimap.put(key, rssiNum);
				} else {
					ArrayList<Integer> rssiNum = new ArrayList<>();
					rssiNum.add(bo.rssi);
					countmap.put(key, 1);
					rssimap.put(key, rssiNum);
				}
			}
		}

		Iterator<String> it = countmap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			int count = countmap.get(key);
			ArrayList<Integer> rssi = rssimap.get(key);
			// 確率計算
			// System.out.println(key+" "+count);
			double probability = (double) count / (double) olist.size();
			bm.setABLEModel(key, probability);
			// System.out.println(key+","+rssi);
			// 変数
			int sum = 0;
			double ave = 0.0;
			double var = 0.0;
			double std = 0.0;
			// 平均計算
			for (int num : rssi) {
				sum = sum + num;
			}
			ave = (double) sum / rssi.size();
			bm.setAverageBLEModel(key, ave);
			// 標準偏差計算
			for (int num : rssi) {
				var += ((num - ave) * (num - ave));
			}
			std = Math.sqrt(var / rssi.size());
			bm.setStdBLEModel(key, std);
			// System.out.println(key+","+ave+","+std);
		}
		// System.out.println(bm.map.size());
		return bm;
	}

	public static String detectRoomByBLE(ArrayList<BLEObservation> blelist, TreeMap<String, BLEModel> blemap) {
		double maxprob = 0.0;
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

	public static double calcProbability(ArrayList<BLEObservation> blelist, BLEModel bm, String roomid) {
		Iterator<String> it = bm.map.keySet().iterator();
		ArrayList<String> listkey = new ArrayList<String>();
		TreeMap<String, Double> probMap = new TreeMap<>();
		double probability = 1;

		for (int i = 0; i < blelist.size(); i++) {
			String key = blelist.get(i).uuid + "_" + blelist.get(i).major + "_" + blelist.get(i).minor;
			listkey.add(key);
			if (bm.avemap.get(key) != null) {
				// System.out.println(key+"aaa");
				double ave = bm.avemap.get(key);
				double std = bm.stdmap.get(key);
				double prob_dens = 0.0;
				double ave_dens = 0.0;
				double prob = 0.0;
				// 受信電波強度の確率密度
				// prob_dens =
				// (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(blelist.get(i).rssi-ave,
				// 2)/(2*Math.pow(std, 2))));
				ProbabilityDensityCalculation pd = new ProbabilityDensityCalculation();
				pd.setData(blelist.get(i).rssi, ave_dens, std);
				prob_dens = pd.getAnswer();
				// NormalDistribution nd = new
				// NormalDistribution(ave,Math.pow(std, 2));
				// prob_dens = nd.frequencyOf(blelist.get(i).rssi);
				// System.out.println(prob_dens);
				// 平均の電波強度の確率密度
				// ave_dens =
				// (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(ave-ave,
				// 2)/(2*Math.pow(std, 2))));
				// 平均の確率密度を１とした時の受信電波強度の確率密度の割合
				// prob = (prob_dens/ave_dens);
				probMap.put(key, prob_dens);
				//System.out.println(key+",rssi"+blelist.get(i).rssi+",ave"+ave+","+prob+"%");
				// System.out.println(key+",rssi"+blelist.get(i).rssi+",ave"+ave+",ave_dens"+ave_dens+","+prob_dens);
				// System.out.println(key+","+prob+"%");
			}

		}

		//System.out.println(listkey);

		while (it.hasNext()) {
			String key = it.next();
			//System.out.println(key);
			// System.out.println(probability);
			if (listkey.indexOf(key) != -1) {
				probability = probability * bm.map.get(key) * probMap.get(key);
				// System.out.println(roomid+":BLEprobability:"+probability*100+"%");
				// System.out.println(probability+","+bm.map.get(key)*probMap.get(key));
				// System.out.println(key+",aa,"+probability);
				//System.out.println(listkey + key + probability);
			} else {
				probability = probability * 0 * bm.map.get(key);
				/*if (probMap.get(key) == null) {
					//System.out.println(key+probability);
					probability = probability * (1 - bm.map.get(key));
					//System.out.println(probability +","+key + bm.map.get(key));
				} else {
					probability = probability * (1 - bm.map.get(key)) * (1 - probMap.get(key));
					//System.out.println(key+probability);
				}*/
				// System.out.println(key+","+bm.map.get(key));
			}
			//System.out.println(key);
		}
		//System.out.println(roomid+":BLEprobability:"+probability*100+"%");
		return probability;
	}

	public static WifiRoomModel generateWifiModel(ArrayList<Observation> olist) {
		WifiRoomModel wm = new WifiRoomModel();

		TreeMap<String, Integer> countmap = new TreeMap<String, Integer>();
		TreeMap<String, ArrayList<Integer>> rssimap = new TreeMap<String, ArrayList<Integer>>();
		for (Observation o : olist) {
			ArrayList<WifiObservation> wolist = o.wifilist;
			for (WifiObservation wo : wolist) {
				String key = WifiRoomModel.generateKey(wo.bssid);
				if (countmap.containsKey(key)) {
					int count = countmap.get(key);
					ArrayList<Integer> rssiNum = rssimap.get(key);
					count++;
					countmap.put(key, count);
					rssiNum.add(wo.rssi);
					rssimap.put(key, rssiNum);
				} else {
					ArrayList<Integer> rssiNum = new ArrayList<>();
					rssiNum.add(wo.rssi);
					countmap.put(key, 1);
					rssimap.put(key, rssiNum);
				}
			}
		}

		Iterator<String> it = countmap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			int count = countmap.get(key);
			// System.out.println(countmap);
			// System.out.println(count);
			ArrayList<Integer> rssi = rssimap.get(key);
			double probability = (double) count / (double) olist.size();
			// System.out.println(key+" = "+count+":"+probability*100+"%");
			wm.setAWifiModel(key, probability);
			// 変数
			int sum = 0;
			double ave = 0.0;
			double var = 0.0;
			double std = 0.0;
			// 平均計算
			for (int num : rssi) {
				sum = sum + num;
			}
			ave = (double) sum / rssi.size();
			wm.setAverageWifiModel(key, ave);
			// 標準偏差計算
			for (int num : rssi) {
				var += ((num - ave) * (num - ave));
			}
			std = Math.sqrt(var / rssi.size());
			wm.setStdWifiModel(key, std);
			// System.out.println(key+","+ave+","+std);

		}
		return wm;
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

	public static double calcProbability(ArrayList<WifiObservation> wifilist, WifiRoomModel wm, String roomid) {
		Iterator<String> it = wm.map.keySet().iterator();
		ArrayList<String> listkey = new ArrayList<String>();
		TreeMap<String, Double> probMap = new TreeMap<>();
		double probability = 1;

		for (int i = 0; i < wifilist.size(); i++) {
			listkey.add(wifilist.get(i).bssid);

			if (wm.avemap.get(wifilist.get(i).bssid) != null) {
				double ave = wm.avemap.get(wifilist.get(i).bssid);
				double std = wm.stdmap.get(wifilist.get(i).bssid);
				double prob_dens = 0.0;
				double ave_dens = 0.0;
				double prob = 0.0;
				prob_dens = (1 / ((Math.sqrt(2 * Math.PI)) * std))
						* Math.exp(-(Math.pow(wifilist.get(i).rssi - ave, 2) / (2 * Math.pow(std, 2))));
				ave_dens = (1 / ((Math.sqrt(2 * Math.PI)) * std))
						* Math.exp(-(Math.pow(ave - ave, 2) / (2 * Math.pow(std, 2))));
				prob = (prob_dens / ave_dens);
				probMap.put(wifilist.get(i).bssid, prob);
				// System.out.println(key+",rssi"+blelist.get(i).rssi+",ave"+ave+","+prob+"%");
				// System.out.println(key+",rssi"+blelist.get(i).rssi+",ave"+ave+",ave_dens"+ave_dens+","+prob_dens);
				// System.out.println(key+","+prob+"%");
			}

		}

		while (it.hasNext()) {
			String key = it.next();
			if (listkey.indexOf(key) != -1) {
				probability = probability * wm.map.get(key) * probMap.get(key);
			} else {
				if (probMap.get(key) == null) {
					probability = probability * (1 - wm.map.get(key));
				} else {
					probability = probability * (1 - wm.map.get(key)) * (1 - probMap.get(key));
				}
			}
		}
		// System.out.println(roomid+":Wifiprobability:"+probability*100+"%");
		return probability;
	}

	public static void print(TreeMap<String, BLEModel> blemap, TreeMap<String, WifiRoomModel> wifimap) {
		Iterator<String> itb = blemap.keySet().iterator();
		while (itb.hasNext()) {
			String roomid = itb.next();
			System.out.println(roomid);
			BLEModel bm = blemap.get(roomid);
			Iterator<String> it = bm.map.keySet().iterator();
			System.out.println("BLE");
			while (it.hasNext()) {
				String key = it.next();
				System.out.println(
						key + ",確率:" + bm.map.get(key) + ",平均:" + bm.avemap.get(key) + ",標準偏差:" + bm.stdmap.get(key));
			}
		}
		Iterator<String> itw = wifimap.keySet().iterator();
		while (itw.hasNext()) {
			String roomid = itw.next();
			WifiRoomModel wm = wifimap.get(roomid);
			System.out.println(wm.map.size());
			Iterator<String> ite = wm.map.keySet().iterator();
			System.out.println("Wi-Fi");
			// System.out.println(wm.map.keySet());
			while (ite.hasNext()) {
				// System.out.println("aa");
				String key = ite.next();
				System.out.println(
						key + ",確率:" + wm.map.get(key) + ",平均:" + wm.avemap.get(key) + ",標準偏差:" + wm.stdmap.get(key));
			}
		}

		/*
		 * Iterator<String> itw = wifimap.keySet().iterator();
		 * while(itw.hasNext()){ String roomid=itw.next();
		 * System.out.println(roomid); WifiModel wm = wifimap.get(roomid);
		 * Iterator<String> it = wm.map.keySet().iterator();
		 * while(it.hasNext()){ String key = it.next();
		 * System.out.println(key+","+wm.map.get(key)+","+wm.avemap.get(key)+","
		 * +wm.stdmap.get(key)); } }
		 */
	}

	public TreeMap<String, ArrayList<String>> getCompMoveHereMap() {
		return this.compMoveHereMap;
	}

	public TreeMap<String, ArrayList<String>> getCompMoveToSomewhereMap() {
		return this.compMoveToSomewhereMap;
	}

	public TreeMap<String, String> getErrorMoveHereDate() {
		return errorMoveHereDateMap;
	}

	public TreeMap<String, String> getErrorMoveToSomewhereDate() {
		return errorMoveToSomewhereDateMap;
	}
}
