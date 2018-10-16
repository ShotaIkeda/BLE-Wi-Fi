package csvreader;

import java.util.ArrayList;

public class Observation {
	String datetime;
	String roomid;
	String bleObservation;
	String wifiObservation;
	ArrayList<BLEObservation> blelist = new ArrayList<BLEObservation>(); 
	ArrayList<WifiObservation> wifilist = new ArrayList<WifiObservation>(); 
	
	public Observation(){
		
	}
	
	public void setDateTime(String datetime){
		this.datetime=datetime; 
	}
	public void setRoomId(String roomid){
		this.roomid=roomid; 
	}
	public void setBLEObservation(String bleObservation){
		this.bleObservation = bleObservation;
	}
	public void setWifiObservation(String wifiObservation){
		this.wifiObservation = wifiObservation;
	}	
	public String getRoomId(){
		return roomid;
	}
	
	public String getDateTime(){
		return datetime;
	}
	
	public void addBLEObservation(BLEObservation o){
		blelist.add(o);
	}
	public void addWifiObservation(WifiObservation o){
		wifilist.add(o);
	}
	public void disp(){
		System.out.println(datetime);
		System.out.println(roomid);
		for(int i=0;i<blelist.size();i++){
			BLEObservation bo = blelist.get(i);
			bo.disp();
		}
		for(int i=0;i<wifilist.size();i++){
			WifiObservation wo = wifilist.get(i);
			wo.disp();
		}
	}
}
