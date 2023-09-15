package SparkProgramming.iKTSpark;

import java.util.ArrayList;
import java.util.Random;



public class iKTTruck {
	double alpha,delta;
	int seed,n0,round,coreID,nCores;
	double muCall, muServe1, muServe2;
	
	int[] I;
	
	double simulationTime = 0d;
	int sampleSize = 0;
	
	
	int[] bestIDs;
	double[]frakX;
	int[] T;
	
	Random R = new Random();
	public iKTTruck() {
		alpha = 0.05;
		delta = 0.1;
		seed = 123;
		n0 = 10;
		round = 2;
		coreID = 0;
		nCores = 8;
		muCall = 6d/60;
		muServe1= 3d/60;
		muServe2 = 2d/60;
		I = new int[10];
	}
	public iKTTruck(double alpha, double delta,int seed,int n0,int round,int coreID,int nCores,double muCall,
			double muServe1,double muServe2,int[] I) {
		this.alpha= alpha;
		this.delta=delta;
		this.seed = seed;
		this.n0=n0;
		this.round = round;
		this.coreID = coreID;
		this.nCores = nCores;
		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.I = I;
	}
	
	public int[] getBestIDs() {
		return bestIDs;
	}
	
	public double[] getFrakX() {
		return frakX;
	}
	
	public int[] getT() {
		return T;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}
	
	public int getSampleSize() {
		return sampleSize;
	}
	
	public void run() {
		ArrayList<Integer> tempBestIDs = new ArrayList<Integer>();
		ArrayList<Integer> tempT = new ArrayList<Integer>();
		ArrayList<Double> tempFrakX = new ArrayList<Double>();
		
		
		ArrayList<Integer> Is = new ArrayList<Integer>();
		int _s = 0;
		Is.add(I[0]);
		int numOfAlt = 1;
		while(_s*2*nCores+coreID*2+1 < I.length) {
			Is.add(I[_s*2*nCores+coreID*2+1]);
			numOfAlt++;
			if(_s*2*nCores+coreID*2+2 < I.length) {
				Is.add(I[_s*nCores*2+coreID*2+2]);
				numOfAlt++;
			}
			_s++;
		}
		double[][] altVec = new double[12376][7]; 
		int _k=0;
		for(int i1 =0;i1<=11;i1++) {
			for(int i2 =i1; i2 <=11;i2++) {
				for(int i3 =i2; i3 <=11;i3++) {
					for(int i4 =i3; i4 <=11;i4++) {
						for(int i5 =i4; i5 <=11;i5++) {
							for(int i6 = i5; i6<=11;i6++) {
								altVec[_k][0]=i1;
								altVec[_k][1]=i2-i1;
								altVec[_k][2]=i3-i2;
								altVec[_k][3]=i4-i3;
								altVec[_k][4]=i5-i4;
								altVec[_k][5]=i6-i5;
								altVec[_k][6]=11-i6;
								_k++;
							}
						}
					}
				}
			}
		}
		long start = System.nanoTime();
		
		
		R.setSeed((long)seed);
		double alpha_r = alpha/Math.pow(2, round);
		ArrayList<Double> refSamples = new ArrayList<Double>();
		
		int numOfMatches = (int)Math.ceil((Is.size()-1)*1.0/2.0);
		
		for(int i = 0; i < numOfMatches; i++) {
			if(i < numOfMatches - 1 ||(i == numOfMatches && numOfMatches==(Is.size()-1)*1.0/2.0)) {
				double[][] groupAltVec = new double[3][7];
				ArrayList<Integer> group = new ArrayList<Integer>();
				group.add(Is.get(0));
				for(int ell=0; ell < 7; ell++) {
					groupAltVec[0][ell] =altVec[Is.get(0)][ell];
				}
				for(int j = 0; j < 2; j++) {
					group.add(Is.get(2*i+j+1));
					for(int ell=0; ell < 7; ell++) {
						groupAltVec[j+1][ell] =altVec[Is.get(2*i+j+1)][ell];
					}
				}
				//refSamples.clear();
				//if(coreID==7) {
					//System.out.println(coreID+" "+refSamples.size());
				//}
				//refSamples.clear();
				KNTruck y = new KNTruck(muCall,muServe1,muServe2,alpha,round,delta,groupAltVec,R.nextInt(),n0,refSamples);
			//	y.setRefSamples();
				y.run();
				simulationTime=simulationTime+y.getSimulationTime();
				
				
				//while(refSamples.size() >1) {
					//refSamples.remove(refSamples.size()-1);
				//}
				
				//for(int ss = refSamples.size(); ss< y.getRefSamples().size();ss++) {
					//refSamples.add(y.getRefSamples().get(ss));
				//}
				
				//if(coreID==7) {
					//System.out.println(coreID+" "+refSamples.size()+" ");
				//}
				
				//refSamples.addAll(y.getRefSamples());
				//if(coreID==3) {
				//	System.out.println(coreID+" "+numOfMatches+" "+group.get(0)+" "+group.get(1)+" "+group.get(2)+" "+y.getSampleSizeWithoutRef());
				//}
				
				sampleSize = sampleSize + y.getSampleSizeWithoutRef();
				
				//System.out.println(coreID+" "+numOfMatches+" "+i+" "+y.getSampleSizeWithoutRef());
				
				if(i==0 && coreID==0) {
					tempBestIDs.add(group.get(y.getBestID()));
					tempFrakX.add(y.getBestAVG());
					tempT.add(y.getBestSampleSize());
				}else {
					if(y.getBestID()!=0) {
						tempBestIDs.add(group.get(y.getBestID()));
						tempFrakX.add(y.getBestAVG());
						tempT.add(y.getBestSampleSize());
					}
				}
			}else {
				double[][] groupAltVec = new double[3][7];
				ArrayList<Integer> group = new ArrayList<Integer>();
				group.add(Is.get(0));
				for(int ell=0; ell < 7; ell++) {
					groupAltVec[0][ell] =altVec[Is.get(0)][ell];
				}
				group.add(Is.get(Is.size()-1));
				for(int ell=0; ell < 7; ell++) {
					groupAltVec[1][ell] =altVec[Is.get(Is.size()-1)][ell];
				}
				
				KNTruck y = new KNTruck(muCall,muServe1,muServe2,alpha,round,delta,groupAltVec,R.nextInt(),n0,refSamples);
				//y.setRefSamples(refSamples);
				y.run();
				
				simulationTime=simulationTime+y.getSimulationTime();
				for(int ss = refSamples.size(); ss< y.getRefSamples().size();ss++) {
					refSamples.add(y.getRefSamples().get(ss));
				}
				
				
				sampleSize = sampleSize + y.getSampleSizeWithoutRef();
				
				if(i==0 && coreID==0) {
					tempBestIDs.add(group.get(y.getBestID()));
					tempFrakX.add(y.getBestAVG());
					tempT.add(y.getBestSampleSize());
				}else {
					if(y.getBestID()!=0) {
						tempBestIDs.add(group.get(y.getBestID()));
						tempFrakX.add(y.getBestAVG());
						tempT.add(y.getBestSampleSize());
					}
				}
			}
		}
		
		
		sampleSize= sampleSize+refSamples.size();
		bestIDs = new int[tempBestIDs.size()];
		frakX=new double[tempFrakX.size()];
		T=new int[tempT.size()];
		for(int i = 0; i < tempBestIDs.size();i++) {
			bestIDs[i]=tempBestIDs.get(i);
			frakX[i]=tempFrakX.get(i);
			T[i]=tempT.get(i);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
