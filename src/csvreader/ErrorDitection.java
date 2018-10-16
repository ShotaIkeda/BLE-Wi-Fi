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

public class ErrorDitection {
	
	//public static int boundary = -50;// 下限電波強度
	//public static int boundary = -70;// 下限電波強度
	//public static int boundary = -75;// 下限電波強度
	//public static int boundary = -90;// 下限電波強度
	public static double PROB_MAX=0.99;
	public static int boundary;
	static String roomname;
	static int moveHereflag = 0;
	static int somewhereflag = 0;
	

	// BLE/WiFiモデルの入れ物を作っておく
	static TreeMap<String, BLEModel> bleMap = new TreeMap<String, BLEModel>();
	static TreeMap<String, WifiRoomModel> wifiMap = new TreeMap<String, WifiRoomModel>();
	//評価用データの入れ物
	static TreeMap<String, ArrayList<Observation>> adMap = new TreeMap<String, ArrayList<Observation>>();
	static ArrayList<String> moveHereDispList  = new ArrayList<String>();
	static ArrayList<String> moveToSomewhereDispList  = new ArrayList<String>();
	
	
	public void ErrorDitection(){
	}
	
	public void setData(TreeMap<String,BLEModel> ble,TreeMap<String,WifiRoomModel> wifi,TreeMap<String,ArrayList<Observation>> ad,int bound){
		bleMap = ble;
		wifiMap = wifi;
		boundary = bound;
		adMap = ad;
	}
	
	public  void Ditection() throws Exception{
		//File learningFile = new File("src/csvreader/allroomdata.csv");
		File roomDetectionResultFile = new File("src/csvreader/roomDetection1_75.csv");

		//File testFile = new File("src/csvreader/exper1.csv");
		File errorMoveHereDetectionResultFile = new File("src/csvreader/errorMHDetection1.txt");
		File errorMoveToSomewhereDetectionResultFile = new File("src/csvreader/errorMTDetection1.txt");
		
		// BLE異常検出(Move here)
		detectAllBLEMoveHere(adMap,bleMap,wifiMap,errorMoveHereDetectionResultFile);
		
		detectAllBLEMoveToSomewhere(adMap,bleMap,wifiMap,errorMoveToSomewhereDetectionResultFile);
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
				//log("----------------------------------------------",out);
				//log("ID:"+count,out);

				//1回の観測で，あるはずのものがない異常の候補を探す
				detectBLEMoveToSomewhere(o,bleMap,wifiMap,moveToSomewhereErrorList,out);

				count++;
			}
		}
		//log("+++++++++++++++++++++++++++++++++++++++++++++++++",out);
		//log("Final Error Candidates:"+moveToSomewhereErrorList,out);
		
		
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
				//log("----------------------------------------------",out);
				//log("ID:"+count,out);
				
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
		
		//log("Correct:"+o.roomid+",BLE:"+rb+",WiFi:"+rw,out );

	
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
			
			if(moveHereflag == 0){
				//moveHereDispList.add(room);
				moveHereDispList.addAll(list);
				//moveHereDispList.add("\n");
				moveHereflag = 1;
			}
			
			if(list.equals(moveHereDispList)){
				//log("別部屋から"+room+"に移動してきてた可能性のあるBLEビーコン:"+list,out);
			}else{
				changeMoveHereDisp(moveHereDispList,list,room);
				//log("別部屋から移動してきてた可能性のあるBLEビーコン:"+displist,out);
			}
		}
	}
		
	public static void detectBLEMoveToSomewhere(Observation o,TreeMap<String, BLEModel> bleMap,TreeMap<String, WifiRoomModel> wifiMap,ArrayList<String> errorList,PrintWriter out){
		
		//部屋推定で調査する部屋を決定
		//BLEモデルに基づいて推定した部屋
		String rb=detectRoomByBLE(o.blelist, bleMap);
		
		//WiFiモデルに基づいて推定した部屋
		String rw=detectRoomByWifi(o.wifilist, wifiMap);

		
		//log("Correct:"+o.roomid+",BLE:"+rb+",WiFi:"+rw,out );

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
			
			if(somewhereflag == 0){
				moveToSomewhereDispList = list;
				somewhereflag = 1;
			}
			
			if(list.equals(moveToSomewhereDispList)){
				//log("別部屋から"+room+"に移動してきてた可能性のあるBLEビーコン:"+list,out);
			}else{
				changeMoveToSomewhereDisp(moveToSomewhereDispList,list,room);
				//log("別部屋から移動してきてた可能性のあるBLEビーコン:"+displist,out);
			}
			
			
			
			//log(room+"からなくなってしまった可能性のあるBLEビーコン:"+list,out);
			
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

	public static void changeMoveHereDisp(ArrayList<String> disp,ArrayList<String> list,String room){
		ArrayList<String> changelist = new ArrayList<String>();
		ArrayList<String> tmplist = new ArrayList<String>();
		ArrayList<String> deletelist = new ArrayList<String>();
		
		changelist.addAll(disp);	
		
		//System.out.println(disp.size()+","+list.size());
		if(disp.size()>=list.size()){
		for(String a : disp){
			for(String b : list){
				if(changelist.indexOf(b) != -1){
					break;
				}else if(a.indexOf(b) == -1){
					changelist.add(b);
				}
			}
		}
		for(String a : list){
			for(String b : disp){
				if(list.indexOf(b) != -1){
					//break;
					tmplist.add(b);
				}else if(a.indexOf(b) == -1){
					//tmplist.add(b);
					break;
				}
			}
		}
		
		}else{
			for(String a : list){
				for(String b : disp){
					if(a.indexOf(b) == -1){
						if(changelist.indexOf(a) == -1){
							changelist.add(a);
						}
					}else if(changelist.indexOf(b) != -1){
						break;
					}
				}	
			}
		}
		
//		for(String a : list){
//			for(String b : disp){
//				if(list.indexOf(b) != -1){
//					//break;
//					tmplist.add(b);
//				}else if(a.indexOf(b) == -1){
//					//tmplist.add(b);
//					break;
//				}
//			}
//		}
		int number=0;
		deletelist.addAll(changelist);
		//System.out.println(changelist+","+tmplist);
		for(int x=0;x<tmplist.size();x++){
			number=deletelist.indexOf(tmplist.get(x));
			if(number>=0){
				deletelist.remove(number);
				if(deletelist.size()>0){
					for(int y=0;y<deletelist.size();y++){
						//System.out.println(deletelist+","+changelist);
						number = changelist.indexOf(deletelist.get(y));
						if(number > -1){
							changelist.remove(number);
						}
					}
				}
			}
		}
		//System.out.println(deletelist);
		
		
		
		if(!moveHereDispList.equals(changelist)){
			//System.out.println("別部屋から移動してきてた可能性のあるBLEビーコン:"+changelist);
			moveHereDispList = changelist;
		}
		//System.out.println(changelist);
		
	}

	public static void changeMoveToSomewhereDisp(ArrayList<String> disp,ArrayList<String> list,String room){
		ArrayList<String> changelist = new ArrayList<String>();
		ArrayList<String> tmplist = new ArrayList<String>();
		ArrayList<String> deletelist = new ArrayList<String>();
		//System.out.println(room+","+disp+" ,"+list);
		
		changelist.addAll(disp);
		
//		for(String a : disp){
//			for(String b : list){
//				if(tmplist.indexOf(b) != -1){
//					break;
//				}else if(a.indexOf(b) == -1){
//					tmplist.add(b);
//				}
//			}
//		}
		
		if(disp.size()>=list.size()){
			for(String a : disp){
				for(String b : list){
					if(changelist.indexOf(b) != -1){
						break;
					}else if(a.indexOf(b) == -1){
						changelist.add(b);
					}
				}
			}
			for(String a : list){
				for(String b : disp){
					if(list.indexOf(b) != -1){
						//break;
						tmplist.add(b);
					}else if(a.indexOf(b) == -1){
						//tmplist.add(b);
						break;
					}
				}
			}
			
			}else{
				for(String a : list){
					for(String b : disp){
						if(a.indexOf(b) == -1){
							if(changelist.indexOf(a) == -1){
								changelist.add(a);
							}
						}else if(changelist.indexOf(b) != -1){
							break;
						}
					}	
				}
			}
		
		int number=0;
		deletelist.addAll(changelist);
		//System.out.println(changelist+","+tmplist);
		for(int x=0;x<tmplist.size();x++){
			number=deletelist.indexOf(tmplist.get(x));
			if(number>=0){
				deletelist.remove(number);
				if(deletelist.size()>0){
					for(int y=0;y<deletelist.size();y++){
						//System.out.println(deletelist+","+changelist);
						number = changelist.indexOf(deletelist.get(y));
						if(number > -1){
							changelist.remove(number);
						}
					}
				}
			}
		}
		
		if(!moveToSomewhereDispList.equals(changelist)){
			//System.out.println("部屋からなくなってしまった可能性のあるBLEビーコン:"+changelist);
			moveToSomewhereDispList = changelist;
		}
		
	}

	public void disp(){
		System.out.println("別部屋から移動してきてた可能性のあるBLEビーコン:"+moveHereDispList);
		System.out.println("部屋からなくなってしまった可能性のあるBLEビーコン:"+moveToSomewhereDispList);
	}
}
