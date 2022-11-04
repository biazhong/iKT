//This class generates observations for the ambulance allocation problem
package section6_2;


import java.util.Random;

public class genAmbulanceObv {
	int seed;
	int sp;
	int ep;
	double muCall;
	double muServe;
	double[][] hLoc = new double[2][2];
	double[][] aLoc = new double[9][2];
	
	double vt = 24d;
	
	double threashold = 1d/3;

	double[] alt = new double[9];
	Random R = new Random();
	double perOfGC;
	
	
	public genAmbulanceObv() {
		seed = 12314;
		sp = 50;
		ep = 2050;
		muCall  = 55.0/60.0;
		muServe = 10.0/60.0;
		R.setSeed((long)seed);
	}
	
	public genAmbulanceObv(int sp, int ep, double muCall, double muServe, int seed) {
		this.sp = sp;
		this.ep = ep;
		this.muCall = muCall;
		this.muServe = muServe;
		this.seed = seed;
		R.setSeed((long)seed);
	}
	
	public double getPerOfGC() {
		return perOfGC;
	}
	
	public void setAlt(double[] alt) {
		this.alt[0]=alt[0];this.alt[1]=alt[1];this.alt[2]=alt[2];this.alt[3]=alt[3];
		this.alt[4]=alt[4];this.alt[5]=alt[5];this.alt[6]=alt[6];this.alt[7]=alt[7];
		this.alt[8]=alt[8];
	}
	
	public void run() {
		hLoc[0][0]=3;hLoc[0][1]=4.5;
		hLoc[1][0]=12;hLoc[1][1]=9;
		
		aLoc[0][0]=3;aLoc[0][1]=3;
		aLoc[1][0]=3;aLoc[1][1]=12;
		aLoc[2][0]=4.5;aLoc[2][1]=7.5;
		aLoc[3][0]=7.5;aLoc[3][1]=4.5;
		aLoc[4][0]=7.5;aLoc[4][1]=7.5;
		aLoc[5][0]=7.5;aLoc[5][1]=10.5;
		aLoc[6][0]=10.5;aLoc[6][1]=7.5;
		aLoc[7][0]=12;aLoc[7][1]=3;
		aLoc[8][0]=12;aLoc[8][1]=12;
		
		double[][] ambulance = new double[4][3]; //0: Next Available Time; 1 for yes 0 for no; 1: x coordinate of the ambulance; 2: y coordinate of the ambulance.
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 9; j++) {
				if(alt[j]>0) {
					ambulance[i][1]=aLoc[j][0];
					ambulance[i][2]=aLoc[j][1];
					alt[j]--;
					break;
				}
			}
		}
		
		int countGC = 0;
		
		double[][] call = new double[ep][5];//0: when a call occurs; 1: x coodinate of the call; 2: y coordinate of the call; 3: response time of the call; 4: when the call finishes.
		for(int i = 0; i < ep; i++) {
			if(i==0) {
				call[i][0]=-muCall*Math.log(1-R.nextDouble());
			}else {
				call[i][0]=call[i-1][0]-muCall*Math.log(1-R.nextDouble());
			}
			call[i][1]=15*R.nextDouble();
			call[i][2]=15*R.nextDouble();
			
			
			//Select an ambulance to serve the patient
			double[] distance = {100000d,100000d,100000d,100000d};
			boolean checkAvailability = false;
			
			double minimalDistance = 100000000000d;
			double minimalAvailableT = 1000000000000d;
			
			int selectedAmbulanceBasedonD = -1;
			int selectedAmbulanceBasedonT = -1;
			int selectedAmbulance = -1;
			for(int j = 0; j < 4; j++) {
				if(ambulance[j][0] < minimalAvailableT) {
					distance[j] = Math.abs(call[i][1]-ambulance[j][1])+Math.abs(call[i][2]-ambulance[j][2]);
					minimalAvailableT = ambulance[j][0];
					selectedAmbulanceBasedonT = j;
				}
				
				if(call[i][0]>ambulance[j][0]) {
					checkAvailability = true;
					distance[j] = Math.abs(call[i][1]-ambulance[j][1])+Math.abs(call[i][2]-ambulance[j][2]);
					if(distance[j]<minimalDistance) {
						minimalDistance = distance[j];
						selectedAmbulanceBasedonD = j;
					}
				}
			}
			
			if(checkAvailability) {
				selectedAmbulance = selectedAmbulanceBasedonD;
			}else {
				selectedAmbulance = selectedAmbulanceBasedonT;
			}
			
			

			//System.out.println(call[i][0]+" "+call[i][1]+" "+call[i][2]+" "+selectedAmbulance);
			
			double distanceBack = 0d;
			
			double distance1 =Math.abs(call[i][1]-hLoc[0][0])+Math.abs(call[i][2]-ambulance[0][1]);
			double distance2 =Math.abs(call[i][1]-hLoc[1][0])+Math.abs(call[i][2]-ambulance[1][1]);
			
			if(distance1 <= distance2) {
				distanceBack = distance1 + Math.abs(ambulance[selectedAmbulance][1]-hLoc[0][0])+Math.abs(ambulance[selectedAmbulance][2]-hLoc[0][1]);
			}else {
				distanceBack = distance2 + Math.abs(ambulance[selectedAmbulance][1]-hLoc[1][0])+Math.abs(ambulance[selectedAmbulance][2]-hLoc[1][1]);
				
			}
			
			
			if(ambulance[selectedAmbulance][0]>call[i][0]) {
				call[i][3] = ambulance[selectedAmbulance][0]+distance[selectedAmbulance]/vt;
				
				call[i][4] = call[i][3]-muServe*Math.log(1-R.nextDouble())+distanceBack/vt;
				ambulance[selectedAmbulance][0]=call[i][4];
			}else {
				call[i][3] = call[i][0]+distance[selectedAmbulance]/vt;
				call[i][4] = call[i][3]-muServe*Math.log(1-R.nextDouble())+distanceBack/vt;
				ambulance[selectedAmbulance][0]=call[i][4];
			}
			
			
			if(i >= sp && call[i][3]-call[i][0] <= threashold) {
				
				countGC++;
			}
		}
		perOfGC=countGC*1.0/(ep-sp);
	}
}
