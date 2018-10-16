package csvreader;

public class ProbabilityDensityCalculation {
	
	static double rssi = 0.0;
	static double ave = 0.0;
	static double std = 0.0;
	
	public void ProbabilityDensityCalculation(){
		
	}
	
	public void setData(double rssi,double ave,double std){
		this.rssi = rssi;
		this.ave = ave;
		this.std = std;
	}
	public double getAnswer(){
		double prob_dens=0.0;
		double max = 0.0;
		double min = 0.0;
		if(rssi<ave){
			min = (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(rssi-ave, 2)/(2*Math.pow(std, 2))));
			max = (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(ave-ave, 2)/(2*Math.pow(std, 2))));
			prob_dens = (max-min)*2;
		}else{
			min = (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(ave-ave, 2)/(2*Math.pow(std, 2))));
			max = (1/((Math.sqrt(2*Math.PI))*std))*Math.exp(-(Math.pow(rssi-ave, 2)/(2*Math.pow(std, 2))));
			prob_dens = (max-min)*2;

		}
		return prob_dens;
	}
}
