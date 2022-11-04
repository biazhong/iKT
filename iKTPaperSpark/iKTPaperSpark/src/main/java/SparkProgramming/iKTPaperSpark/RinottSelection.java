package SparkProgramming.iKTPaperSpark;

import java.util.Random;

public class RinottSelection {
	double muCall,muServe1,muServe2,alpha,delta;
	int nCores,maxRound,n0Prime,altID,seed;
	
	double sampleAVG=0d;
	int sampleSize = 0;
	double simulationTime = 0d;
	
	Random R = new Random();
	public RinottSelection(){
		muCall = 6d/60;
		muServe1=4d/60d;
		muServe2 = 3d/60d;
		alpha = 0.05;
		delta = 0.001;
		nCores = 8;
		maxRound = 10;
		n0Prime = 20;
		altID = 323;
		seed = 147788;
	}
	
	public RinottSelection(double muCall, double muServe1, double muServe2,
			double alpha, double delta,int nCores,int maxRound,
			int altID, int seed,int n0Prime){
		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.alpha = alpha;
		this.delta = delta;
		this.nCores = nCores;
		this.maxRound = maxRound;
		this.n0Prime = n0Prime;
		this.altID = altID;
		this.seed = seed;
	}
	
	public double getSampleAVG() {
		return sampleAVG;
	}
	
	public int getSampleSize() {
		return sampleSize;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}
	
	public void run() {
		double sampleAVG = 0d;
		double[] tempAlt = new double[7];
		double[] tempX = new double[n0Prime];
		int count = 0;
		for(int i1 =0;i1<=11;i1++) {
			for(int i2 =i1; i2 <=11;i2++) {
				for(int i3 =i2; i3 <=11;i3++) {
					for(int i4 =i3; i4 <=11;i4++) {
						for(int i5 =i4; i5 <=11;i5++) {
							for(int i6 = i5; i6<=11;i6++) {
								if(count==altID) {
									tempAlt[0]=i1;
									tempAlt[1]=i2-i1;
									tempAlt[2]=i3-i2;
									tempAlt[3]=i4-i3;
									tempAlt[4]=i5-i4;
									tempAlt[5]=i6-i5;
									tempAlt[6]=11-i6;
								}
								count++;
							}
						}
					}
				}
			}
		}
		R.setSeed((long)seed);
		genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,R.nextInt());
		genObv.setAlt(tempAlt);
		double _s = System.nanoTime();
		for(int j = 0 ; j < n0Prime; j ++) {
			genObv.run();
			tempX[j]=genObv.getAVGWaitingTime();
			sampleAVG = sampleAVG + tempX[j];
		}
		simulationTime = simulationTime + System.nanoTime()-_s;
		int rinottH =(int)Math.ceil(Rinott.rinott(nCores, 1-alpha/Math.pow(2,maxRound), n0Prime-1));
		double S2 = 0;
		for(int j=0;j<n0Prime;j++) {
			S2 = S2 + (tempX[j]-sampleAVG/n0Prime)*(tempX[j]-sampleAVG/n0Prime);
		}
		S2 = S2 / (n0Prime - 1);
		sampleSize = (int)Math.ceil(S2*rinottH*rinottH/(delta*delta));
		_s = System.nanoTime();
		for(int j = 0 ; j < sampleSize - n0Prime; j ++) {
			genObv.run();
			sampleAVG = sampleAVG + genObv.getAVGWaitingTime();
		}
		simulationTime = simulationTime + System.nanoTime()-_s;
		sampleAVG = sampleAVG/sampleSize;
	}
}
