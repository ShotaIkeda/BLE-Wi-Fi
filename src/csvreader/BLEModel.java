package csvreader;

import java.util.TreeMap;

public class BLEModel {
	TreeMap<String,Double> map = new TreeMap<String,Double>();
	TreeMap<String,Double> avemap = new TreeMap<String,Double>();
	TreeMap<String,Double> stdmap = new TreeMap<String,Double>();
	
	
	public BLEModel(){
		
	}
	
	public void setABLEModel(String key,double probability){
		map.put(key, probability);		
	}
	
	public void setAverageBLEModel(String key,double average){
		avemap.put(key, average);		
	}
	
	public void setStdBLEModel(String key,double std){
		stdmap.put(key, std);		
	}
	
	public static String generateKey(String uuid,int major,int minor){
		String key = uuid+"_"+major+"_"+minor;
		return key;
	}
}
