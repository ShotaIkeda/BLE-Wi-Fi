package csvreader;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class AutoUpdateModel {

	static TreeMap<String, ArrayList<String>> compMoveHereMap;
	static TreeMap<String, ArrayList<String>> compMoveToSomewhereMap;
	static TreeMap<String, String> errorMoveHereDateMap;
	static TreeMap<String, String> errorMoveToSomewhereDateMap;
	static Observation o;
	static String room="";
	
	AutoUpdateModel(){	
	}
	
	public void selectProcess(String room, Observation o) throws IOException {
		this.o = o;
		this.room = room;
		
		//メンテナンス情報があるか
		boolean bl = checkMentenanceFile();
		if (bl == true) {
			newCreateModel();
			logFileWrite();
			System.out.println("a");
		} else {
			standByProcessing();
			System.out.println("b");
		}
	}

	public static void newCreateModel() {
		String room = "";
		try {
			File file = new File("mentenanceRoom.txt");
			File file_SaveData = new File("saveData.txt");

			FileReader filereader = new FileReader(file);
			boolean bl = checkSaveFile();

			//保存してあった観測データを削除
			
			if (bl == true) {
				file_SaveData.delete();
				BufferedReader br = new BufferedReader(filereader);
				String str = "";
				//日付データに書き込み
				while ((str = br.readLine()) != null) {
					StringTokenizer token;
					token = new StringTokenizer(str, ",");
					while (token.hasMoreTokens()) {
						String roomName = token.nextToken();
						String date = token.nextToken();
						// System.out.println(roomName+","+date);
						FileWriter fw = new FileWriter(new File(roomName + "mentenanceDate.txt"), true);
						fw.write(date + "\n");
						fw.close();
					}
				}
			
			} else {
				BufferedReader br = new BufferedReader(filereader);
				String str = "";
				//日付データに書き込み
				while ((str = br.readLine()) != null) {
					StringTokenizer token;
					token = new StringTokenizer(str, ",");
					while (token.hasMoreTokens()) {
						String roomName = token.nextToken();
						String date = token.nextToken();
						// System.out.println(roomName+","+date);
						FileWriter fw = new FileWriter(new File(roomName + "mentenanceDate.txt"), true);
						fw.write(date + "\n");
						fw.close();
					}
				}
			}
			//該当する部屋のエラー状況のデータを削除
			
			
			//モデルに書き込み
			WriteRoomModel wm = new WriteRoomModel(room,o);
			filereader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void standByProcessing() throws IOException {
		//待機処理があるか
		if (checkWaitProcess() == true) {
			//不具合が全て消えたか
			boolean check = checkErrorDelete();
			if (check == true) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(new File("roomName" + "waitProcess.txt")));
					String str = "";
					while ((str = br.readLine()) != null) {
						StringTokenizer token;
						token = new StringTokenizer(str, ",");

						String date = token.nextToken();
						String roomName = token.nextToken();
						String ble = token.nextToken();
						String wifi = token.nextToken();

						Observation o = new Observation();
						o.setDateTime(date);
						o.setRoomId(roomName);
						o.setBLEObservation(ble);
						o.setWifiObservation(wifi);

						WriteRoomModel wrm = new WriteRoomModel(roomName, o);
					}
				} catch (IOException e) {
					System.out.println(e.toString());
				}
			} else {
				try {
					//待機処理に観測データを追加
					File waitProcess = new File(room + "waitProcess.txt");
					FileWriter fw = new FileWriter(waitProcess, true);
					fw.write(o.datetime + "," + room + "," + o.bleObservation + "," + o.wifiObservation + "\n");
					fw.close();
					
					//部屋ごとのエラー情報の更新
					
				} catch (IOException e) {
				}
			}
		} else {
			try {
				//待機処理に観測データを追加
				File waitProcess = new File(room + "waitProcess.txt");
				FileWriter fw = new FileWriter(waitProcess, true);
				fw.write(o.datetime + "," + room + "," + o.bleObservation + "," + o.wifiObservation + "\n");
				fw.close();
				
				//部屋ごとのエラー状況を保存
				
				
			} catch (IOException e) {

			}
		}
	}

	public static boolean checkErrorDelete() {
		File file = new File(room+ "errorDelete.txt");
		if (file.exists()) {
			//エラー状況の比較
			
			return true;
		} else {
			
			return false;
		}
	}

	public static void logFileWrite() {
		File file = new File("mentenanceRoom.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = "";
			while ((str = br.readLine()) != null) {
				FileWriter fw = new FileWriter(new File("mentenanceLog.txt"), true);
				fw.write(str + "\n");
				fw.close();
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		file.delete();
	}

	public static boolean checkWaitProcess() {

		File file = new File(room + "waitProcess.txt");
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkMentenanceFile() {
		File file = new File("mentenanceRoom.txt");
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkSaveFile() {

		File file = new File("SaveDate.txt");
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public void setErrorData(TreeMap<String,ArrayList<String>> compMoveHereMap,TreeMap<String,ArrayList<String>> compMoveToSomewhereMap){
		this.compMoveHereMap = compMoveHereMap;
		this.compMoveToSomewhereMap = compMoveToSomewhereMap;
	}
}
