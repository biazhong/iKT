package SparkProgramming.iKTPaperSpark;


import java.util.Random;

public class genTruckObv {
	int seed;
	double muCall;
	double muServe1;
	double muServe2;
	double[][] wLoc = new double[7][2];
	
	double vt = 20d;
	
	double avgWT;
	
	double[] alt = new double[7];
	Random R = new Random();
	public genTruckObv() {
		seed = 12314;
		muCall  = 6.0/60.0;
		muServe1 = 3.0/60.0;
		muServe2 = 2.0/60.0;
		R.setSeed((long)seed);
	}
	public genTruckObv(double muCall, double muServe1, double muServe2, int seed) {
		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.seed = seed;
		R.setSeed((long)seed);
	}
	
	public double getAVGWaitingTime() {
		return -avgWT;
	}
	
	public void setAlt(double[] alt) {
		this.alt[0]=alt[0];this.alt[1]=alt[1];this.alt[2]=alt[2];
		this.alt[3]=alt[3];this.alt[4]=alt[4];this.alt[5]=alt[5];
		this.alt[6]=alt[6];
	}
	
	public void run() {
		avgWT=0d;
		wLoc[0][0]=3;wLoc[0][1]=6;
		wLoc[1][0]=3;wLoc[1][1]=12;
		wLoc[2][0]=7.5;wLoc[2][1]=7.5;
		wLoc[3][0]=7.5;wLoc[3][1]=12;
		wLoc[4][0]=7.5;wLoc[4][1]=3;
		wLoc[5][0]=12;wLoc[5][1]=9;
		wLoc[6][0]=12;wLoc[6][1]=3;
		double[][] truck = new double[11][3]; //0: Next Available Time; 1 for yes 0 for no; 1: x coordinate of the ambulance; 2: y coordinate of the ambulance.
		for(int i = 0; i < 11; i++) {
			for(int j = 0; j < 7; j++) {
				if(alt[j]>0) {
					truck[i][1]=wLoc[j][0];
					truck[i][2]=wLoc[j][1];
					alt[j]--;
					break;
				}
			}
		}
		double latestCallTime = 0d;
		double[][] call = new double[1000][5];//0: when a call occurs; 1: x coodinate of the call; 2: y coordinate of the call; 3: response time of the call; 4: when the call finishes.
		int i = 0;
		while(latestCallTime <= 12) {
			if(i==0) {
				call[i][0]=-muCall*Math.log(1-R.nextDouble());
				latestCallTime=call[i][0];
			}else {
				call[i][0]=call[i-1][0]-muCall*Math.log(1-R.nextDouble());
				latestCallTime=call[i][0];
			}
			call[i][1]=15*R.nextDouble();
			call[i][2]=15*R.nextDouble();
			
			
			double[] distance = {100000d,100000d,100000d,100000d,100000d
					,100000d,100000d,100000d,100000d,100000d,100000d};
			boolean checkAvailability = false;
			
			double minimalDistance = 100000000000d;
			double minimalAvailableT = 1000000000000d;
			
			int selectedTruckBasedonD = -1;
			int selectedTruckBasedonT = -1;
			int selectedTruck = -1;
			for(int j = 0; j < 11; j++) {
				if(truck[j][0] < minimalAvailableT) {
					distance[j] = Math.abs(call[i][1]-truck[j][1])+Math.abs(call[i][2]-truck[j][2]);
					minimalAvailableT = truck[j][0];
					selectedTruckBasedonT = j;
				}
				
				if(call[i][0]>truck[j][0]) {
					checkAvailability = true;
					distance[j] = Math.abs(call[i][1]-truck[j][1])+Math.abs(call[i][2]-truck[j][2]);
					if(distance[j]<minimalDistance) {
						minimalDistance = distance[j];
						selectedTruckBasedonD = j;
					}
				}
			}
			if(checkAvailability) {
				selectedTruck = selectedTruckBasedonD;
			}else {
				selectedTruck = selectedTruckBasedonT;
			}
			
			double distanceBack = Math.abs(call[i][1]-truck[selectedTruck][1])+Math.abs(call[i][2]-truck[selectedTruck][2]);
			if(truck[selectedTruck][0]>call[i][0]) {
				call[i][3] = truck[selectedTruck][0]-muServe1*Math.log(1-R.nextDouble())+distance[selectedTruck]/vt-muServe2*Math.log(1-R.nextDouble());
				
				call[i][4] = call[i][3]+distanceBack/vt;
				truck[selectedTruck][0]=call[i][4];
			}else {
				call[i][3] = call[i][0]-muServe1*Math.log(1-R.nextDouble())+distance[selectedTruck]/vt-muServe2*Math.log(1-R.nextDouble());
				call[i][4] = call[i][3]+distanceBack/vt;
				truck[selectedTruck][0]=call[i][4];
			}
			//System.out.println(call[i][0]+" "+call[i][1]+" "+call[i][2]+" "+distance[selectedTruck]+" "+call[i][3]+" "+call[i][4]);
			avgWT = avgWT + call[i][3]-call[i][0];
			i++;
		}
		//System.out.println(i);
		avgWT=avgWT/i;
	}
}
