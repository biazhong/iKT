package SparkProgramming.iKTSpark;

import java.util.Random;


public class iKT_INI_STAGE{
	int seed = 1374;
	int n0Frak;
	double muCall, muServe1, muServe2;
	double simulationTime = 0d;
	int maxRound = 1;
	int coreID;
	int nCores;
	int[] I;
	double[] frakX;
	
	
	Random R = new Random();
	
	public iKT_INI_STAGE () {
		seed = 123;
		n0Frak = 10;
		muCall = 6d/60;
		muServe1= 3d/60;
		muServe2 = 2d/60;
		coreID = 0;
		nCores=8;
	}
	public iKT_INI_STAGE (double muCall, double muServe1, double muServe2,int coreID, int nCores, int seed, int n0Frak) {

		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.coreID = coreID;
		this.nCores = nCores;
		this.seed = seed;
		this.n0Frak  = n0Frak;
	}
	
	public double[] getFrakX() {
		return frakX;
	}
	
	public int[] getSetOfAlt() {
		return I;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}
	
	public void run() {
		int k = 0;
		for(int i = 0 ; i < 12376;i++) {
			if(i % nCores == coreID) {
				k++;
			}
		}
		double[][] altVec = new double[k][7]; 
		
		
		double[] tempAlt = new double[7];
		R.setSeed((long)seed);
		genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,R.nextInt());
		
		frakX = new double[k];
		I = new int[k];
		int numOfAlt = 0;
		int count = 0;
		for(int i1 =0;i1<=11;i1++) {
			for(int i2 =i1; i2 <=11;i2++) {
				for(int i3 =i2; i3 <=11;i3++) {
					for(int i4 =i3; i4 <=11;i4++) {
						for(int i5 =i4; i5 <=11;i5++) {
							for(int i6 = i5; i6<=11;i6++) {
								if(count%nCores == coreID) {
									altVec[numOfAlt][0]=i1;
									altVec[numOfAlt][1]=i2-i1;
									altVec[numOfAlt][2]=i3-i2;
									altVec[numOfAlt][3]=i4-i3;
									altVec[numOfAlt][4]=i5-i4;
									altVec[numOfAlt][5]=i6-i5;
									altVec[numOfAlt][6]=11-i6;
									
									I[numOfAlt]=count;
									numOfAlt++;
								}
									count++;
							}
						}
					}
				}
			}
		}
		
		double _s = System.nanoTime();
		System.nanoTime();
		for(int i = 0 ; i < k; i++) {
			for(int j = 0 ; j < n0Frak; j ++) {
				for(int ell=0; ell < 7; ell++) {
					tempAlt[ell] =altVec[i][ell];
				}
				genObv.setAlt(tempAlt);
				genObv.run();
				frakX[i]=frakX[i]+genObv.getAVGWaitingTime();
			}
		}
		
		for(int i = 0 ; i < k; i++) {
			frakX[i]=frakX[i]/n0Frak;
		}
		
		simulationTime = simulationTime + System.nanoTime()-_s;
	}
}
