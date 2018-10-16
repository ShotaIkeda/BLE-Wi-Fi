package csvreader;

public class ObservationDatabase {
	String id;
	String terminalId;
	String date;
	String bleObservation;
	String wifiObservation;
	
	public ObservationDatabase(String id,String terminalId,String date,String bleObservation,String wifiObservation){
		this.id = id;
		this.terminalId = terminalId;
		this.date = date;
		this.bleObservation = bleObservation;
		this.wifiObservation = wifiObservation;
	}
	public void disp(){
		System.out.println(id+","+terminalId+","+date+","+bleObservation+","+wifiObservation);
	}
}
