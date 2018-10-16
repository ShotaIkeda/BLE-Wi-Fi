package csvreader;


public class WifiObservation {
	String ssid;
	String bssid;
	int rssi;
	
	public WifiObservation(String ssid,String bssid,int rssi){

		this.ssid = ssid;
		this.bssid = bssid;
		this.rssi = rssi;
		
	}
	
	public void disp(){
		System.out.println("Wifi:"+ssid+","+bssid+","+rssi);
	}
}
