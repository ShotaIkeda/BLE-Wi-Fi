package csvreader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class WriteMentenanceDate {
    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	WriteMentenanceDate(String roomName){
		try {
            FileWriter fw = new FileWriter("mentenanceRoom.txt",true);
            Date date = new Date();
            fw.write(roomName+","+sdf1.format(date)+"\n");
            fw.close();
            System.out.println("succes");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	WriteMentenanceDate(String roomName,String date){
		try {
            FileWriter fw = new FileWriter("mentenanceRoom.txt",true);
            fw.write(roomName+","+sdf1.format(date)+"\n");
            fw.close();
            System.out.println("succes");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
}
