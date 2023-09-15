package SparkProgramming.iKTSpark;

import java.util.ArrayList;
import java.util.Random;



public class KNTruckCRN {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	int n0Prime;
	int round;
	double muCall, muServe1,muServe2;
	double simulationTime=0;
	
	double[][] altVec;
	
	ArrayList<Double> refSamples;
	public KNTruckCRN() {
		alpha = 0.05;
		round =1;
		delta = 0.1;
		seed = 123;
		n0Prime = 10;
		muCall = 6d/60;
		muServe1 = 3d/60;
		muServe2 = 2d/60;
	}
	public KNTruckCRN(double muCall, double muServe1,double muServe2,
			double alpha,int round, double delta,double[][] altVec, int seed, 
			int n0Prime,ArrayList<Double> refSamples) {
		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.alpha = alpha;
		this.round = round;
		this.delta = delta;
		this.altVec = altVec;
		this.seed = seed;
		this.n0Prime  = n0Prime;
		long start = System.nanoTime();
		this.refSamples = refSamples;
		simulationTime=simulationTime+System.nanoTime()-start;
	}
	
	//Return best alternative
	public int getBestID() {
		return bestID;
	}
	
	
	//Return total sample size (include the sample size of the reference alternative)
	public int getSampleSize() {
		return sampleSize+refSampleSize;
	}
	
	//Return total sample size (exclude the sample size of the reference alternative)
	public int getSampleSizeWithoutRef() {
		return sampleSize;
	}
	
	//Return the sample average of the best alternative
	public double getBestAVG(){
		return bestAVG;
	}
	
	//Return the sample size of the best alternative
	public int getBestSampleSize() {
		return bestSampleSize;
	}
	
	//Initiate an array that records the samples of the reference alternative
	public void setRefSamples(ArrayList<Double> samples) {
		refSamples.addAll(samples);
	}
	
	//Return the sample size of the reference alternative
	public ArrayList<Double> getRefSamples(){
		return refSamples;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}
	//While conducting the selection, the reference alternative is the 1st alternative in set I.
	public void run() {
		alpha = alpha/Math.pow(2, round);
		double[] tempAlt = new double[7];
		
		
		
		
		
	
		
		
		ArrayList<Integer> I = new ArrayList<Integer>();
		
		

		
		
		int k = altVec.length;
		for(int i = 0 ; i < k; i++) {
			I.add(i);
		}
		double[] X = new double[I.size()];
		
		double[][] tempX = new double[I.size()][n0Prime];
		double[][] S2 = new double[I.size()][I.size()];
		

		double hr2 = (n0Prime - 1)*(Math.pow(2*alpha/(k-1), -2.0/(n0Prime-1))-1)/(2*delta);
		for(int t = 0;  t < n0Prime;t++) {
			for(int i=0; i < I.size(); i++) {
				if(i==0) {
					if(refSamples.size()>t) {
						X[0] = X[0] + refSamples.get(t);
						tempX[i][t]=refSamples.get(t);
					}else {
						for(int ell=0; ell < 7; ell++) {
							tempAlt[ell] =altVec[i][ell];
						}
						double _s = System.nanoTime();
						
						genTruckObv genObvRef = new genTruckObv(muCall,muServe1,muServe2,seed+t);
						genObvRef.setAlt(tempAlt);
						genObvRef.run();
						refSamples.add(genObvRef.getAVGWaitingTime());
						simulationTime = simulationTime + System.nanoTime()-_s;
						
						X[i] = X[i] + refSamples.get(t);
						tempX[i][t]=refSamples.get(t);
					}
				}else {
					for(int ell=0; ell < 7; ell++) {
						tempAlt[ell] =altVec[i][ell];
					}
					double _s = System.nanoTime();
					genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,seed+t);
					genObv.setAlt(tempAlt);
					genObv.run();
					
					
					tempX[i][t]=genObv.getAVGWaitingTime();
					
					X[i] = X[i] + tempX[i][t];
					simulationTime = simulationTime + System.nanoTime()-_s;
					
				}
			}
		}
		
		for(int i = 0 ; i <I.size(); i++) {
			for(int j = i + 1; j < I.size(); j++) {
				double tempS2 = 0;
				double tempAVGDiff = X[i]/n0Prime - X[j]/n0Prime;
				for(int count = 0 ; count < n0Prime; count++) {
					tempS2 =  tempS2 + (tempX[i][count]-tempX[j][count]-tempAVGDiff)* (tempX[i][count]-tempX[j][count]-tempAVGDiff);
				}
				tempS2 = tempS2 / (n0Prime - 1.0);
				S2[i][j]=tempS2;
				S2[j][i]=tempS2;
			}
		}
		int t = n0Prime;
		while(I.size()>1) {
			for(int i = 0 ; i < I.size(); i++) {
				boolean checkEli = false;
				//if(I.get(i)==20) {
				//	System.out.println("Sample Average: "+I.get(i)+" "+X[I.get(i)]/t);
				//}
				for(int j = i + 1 ; j < I.size(); j++) {
					
					double boundary=hr2*S2[I.get(i)][I.get(j)] - delta * t/2;
					double Zij = X[I.get(i)]-X[I.get(j)];
					if((boundary > 0&&Zij>boundary)||(boundary<0&&Zij>0)) {
						
						if(I.get(j)!=0) {
							sampleSize =sampleSize + t;
						}else {
							refSampleSize = t;
						}
						I.remove(j);						
						j--;
						
					}
					
					if((boundary > 0&&Zij<-boundary)||(boundary<0&&Zij<0)) {
						
						
						checkEli = true;
						
					}
				}
				
				if(checkEli && I.size()>1) {
					
					if(I.get(0)!=0) {
						sampleSize = sampleSize + t;
					}else {
						refSampleSize = t;
					}
					I.remove(i);
				}
			}
			for(int i = 0 ; i < I.size(); i++) {
				if(I.get(i)==0) {
					if(refSamples.size()>t) {
						X[I.get(i)] = X[I.get(i)] + refSamples.get(t);
					}else {
						for(int ell=0; ell < 7; ell++) {
							tempAlt[ell] =altVec[I.get(i)][ell];
						}
						double _s = System.nanoTime();

						genTruckObv genObvRef = new genTruckObv(muCall,muServe1,muServe2,seed+t);
						genObvRef.setAlt(tempAlt);
						genObvRef.run();
						
						refSamples.add(genObvRef.getAVGWaitingTime());
						X[I.get(i)] = X[I.get(i)] + refSamples.get(t);
						simulationTime = simulationTime+System.nanoTime()-_s;
					}
				}else {
					for(int ell=0; ell < 7; ell++) {
						tempAlt[ell] =altVec[I.get(i)][ell];
					}
					double _s = System.nanoTime();
					genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,seed+t);
					genObv.setAlt(tempAlt);
					genObv.run();
					
					X[I.get(i)] = X[I.get(i)] + genObv.getAVGWaitingTime();
					simulationTime = simulationTime + System.nanoTime()-_s;
				}	
			}
			t++;
		}
		if(I.get(0)!=0) {
			sampleSize = sampleSize + t;
		}else {
			refSampleSize = t;
		}
		bestAVG = X[I.get(0)]/t;
		bestSampleSize = t;
		bestID = I.get(0);
	}
	
}
