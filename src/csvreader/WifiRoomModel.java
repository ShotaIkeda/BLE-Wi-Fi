package csvreader;

import java.util.TreeMap;

public class WifiRoomModel {
	TreeMap<String,Double> map = new TreeMap<String,Double>();
	TreeMap<String,Double> avemap = new TreeMap<String,Double>();
	TreeMap<String,Double> stdmap = new TreeMap<String,Double>();
	
	public WifiRoomModel(){
		
	}
	
	public void setAWifiModel(String key,double probability){
		map.put(key, probability);
	}
	
	public void setAverageWifiModel(String key,double average){
		avemap.put(key, average);		
	}
	
	public void setStdWifiModel(String key,double std){
		stdmap.put(key, std);		
	}
	
	public static String generateKey(String bssid){
		String key = bssid;
		return key;
	}
}
