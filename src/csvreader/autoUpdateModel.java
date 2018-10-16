package csvreader;

import java.io.*;
import java.util.StringTokenizer;

public class autoUpdateModel {

	autoUpdateModel() {
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
		try {
			File file = new File("mentenanceRoom.txt");
			File file_SaveData = new File("saveData.txt");

			FileReader filereader = new FileReader(file);
			boolean bl = checkSaveFile();

			if (bl == true) {
				file_SaveData.delete();
				BufferedReader br = new BufferedReader(filereader);
				String str="";
				while ((str = br.readLine())!= null) {
					StringTokenizer token;
					token = new StringTokenizer(str, ",");
					while (token.hasMoreTokens()) {
						String roomName = token.nextToken();
						String date = token.nextToken();
						//System.out.println(roomName+","+date);
						FileWriter fw = new FileWriter(new File(roomName+"mentenanceDate.txt"),true);
						fw.write(date+"\n");
						fw.close();
					}
				}
			} else {
				BufferedReader br = new BufferedReader(filereader);
				String str="";
				while ((str = br.readLine())!= null) {
					StringTokenizer token;
					token = new StringTokenizer(str, ",");
					while (token.hasMoreTokens()) {
						String roomName = token.nextToken();
						String date = token.nextToken();
						//System.out.println(roomName+","+date);
						FileWriter fw = new FileWriter(new File(roomName+"mentenanceDate.txt"),true);
						fw.write(date+"\n");
						fw.close();
					}
				}
			}
			filereader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void standByProcessing() {
		
	}

	public static void logFileWrite(){
		File file = new File("mentenanceRoom.txt");
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str="";
			while ((str = br.readLine())!= null) {
				FileWriter fw = new FileWriter(new File("mentenanceLog.txt"),true);
				fw.write(str+"\n");	
				fw.close();
			}
		}catch(IOException e){
			System.out.println(e.toString());
		}
		file.delete();
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
}
