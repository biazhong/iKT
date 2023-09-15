package SparkProgramming.iKTSpark;

import java.util.ArrayList;
import java.util.Random;

public class KTTruck {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	int n0Prime;
	double muCall, muServe1, muServe2;
	int totalNumberOfMatches = 0;
	double simulationTime = 0d;
	int maxRound = 1;
	int coreID;
	int nCores;
	
	
	Random R = new Random();
	
	public KTTruck() {
		alpha = 0.05;
		delta = 0.1;
		seed = 123;
		n0Prime = 10;
		muCall = 6d/60;
		muServe1= 3d/60;
		muServe2 = 2d/60;
		coreID = 0;
		nCores=8;
	}
	
	public KTTruck(double muCall, double muServe1, double muServe2,
			double alpha, double delta,int coreID, int nCores, int seed, 
			int n0Prime) {

		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.alpha = alpha;
		this.delta = delta;
		this.coreID = coreID;
		this.nCores = nCores;
		this.seed = seed;
		this.n0Prime  = n0Prime;
	}
	public int getBestID() {
		return bestID;
	}
	
	public int getSampleSize() {
		return sampleSize;
	}
	
	public int getTotalMatches() {
		return totalNumberOfMatches;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}
	public int getMaxRound() {
		return maxRound;
	}
	public void run() {
		int k = 0;
		for(int i = 0 ; i < 12376;i++) {
			if(i % nCores == coreID) {
				k++;
			}
		}
		double[][] altVec = new double[k][7]; 
		
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
									numOfAlt++;
								}
									count++;
							}
						}
					}
				}
			}
		}
		double[] tempAlt = new double[7];
		R.setSeed((long)seed);
		genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,R.nextInt());
		ArrayList<Integer> I = new ArrayList<Integer>();
		for(int i = 0 ; i < k; i++) {
			I.add(i);
		}
		int round = 1;
		//double compareT =0d;
		while(I.size()>1) {
			ArrayList<Integer> tempI = new ArrayList<Integer>();
			double alpha_r = alpha/Math.pow(2, round);
			int numOfMatches = (int)Math.floor(I.size()*1.0/2.0);
			totalNumberOfMatches = totalNumberOfMatches+numOfMatches;
			if(I.size()*1.0/2.0>numOfMatches) {
				tempI.add(I.get(I.size()-1));
				I.remove(I.size()-1);
			}
			for(int i = 0;  i < numOfMatches; i++) {
				//if(coreID ==0) {
					//System.out.println("match"+i);
				//}
				
				double[][] groupAltVec = new double[2][7];
				ArrayList<Integer> group = new ArrayList<Integer>();
				int sample = (int)Math.floor(R.nextDouble()*I.size());
				group.add(I.get(sample));
				//if(coreID ==0) {
					//System.out.println("Start");
					//}
				for(int ell=0; ell < 7; ell++) {
					groupAltVec[0][ell] =altVec[I.get(sample)][ell];
					//if(coreID ==0) {
					//System.out.print(groupAltVec[0][ell]+" ");
					//}
				}
				//System.out.println(" ");
				I.remove(sample);
				sample = (int)Math.floor(R.nextDouble()*I.size());
				group.add(I.get(sample));
				for(int ell=0; ell < 7; ell++) {
					groupAltVec[1][ell] =altVec[I.get(sample)][ell];
					//if(coreID ==0) {
					//System.out.print(groupAltVec[1][ell]+" ");
					//}
				}
				//System.out.println(" ");
				I.remove(sample);
				//System.out.println(muCall+" "+muServe1+" "+muServe2+" "+alpha_r+" "+delta+" "+n0Prime);
				ArrayList<Double> temp = new ArrayList<Double>();
				KNTruck y = new KNTruck(muCall,muServe1,muServe2,alpha,round,delta,groupAltVec,R.nextInt(),n0Prime,temp);
				y.run();
				simulationTime=simulationTime+y.getSimulationTime();
				sampleSize = sampleSize + y.getSampleSize();
				tempI.add(group.get(y.getBestID()));
				
			}
			I.addAll(tempI);
			
			maxRound = round;
			round++;
		}
		System.out.println(maxRound);
		bestID=I.get(0)*nCores+coreID;
	}
}