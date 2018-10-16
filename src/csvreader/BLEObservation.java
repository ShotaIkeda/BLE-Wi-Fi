package csvreader;

public class BLEObservation {
	String uuid;
	int major;
	int minor;
	int rssi;
	
	public BLEObservation(String uuid,int major,int minor,int rssi){
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.rssi = rssi;
	}
	public void disp(){
		System.out.println("BLE:"+uuid+","+major+","+minor+","+rssi);
	}
	
	public boolean equals(Object o){
		if(o instanceof BLEObservation){
			BLEObservation b=(BLEObservation)o;
			if(b.uuid.equals(this.uuid) && b.major==this.major && b.minor==this.minor){
				return true;
			}
		}
		return false;
	}
}
