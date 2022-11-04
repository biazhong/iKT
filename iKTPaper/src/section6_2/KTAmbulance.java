package section6_2;

import java.util.ArrayList;
import java.util.Random;



public class KTAmbulance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	int n0Prime;
	int sp,ep;
	double muCall, muServe;
	int totalNumberOfMatches = 0;
	
	double[][] altVec;
	
	Random R = new Random();
	
	public KTAmbulance() {
		alpha = 0.05;
		delta = 0.1;
		seed = 123;
		n0Prime = 10;
		sp = 1000;
		ep = 2000;
		muCall = 55d/60;
		muServe = 10d/60;
	}
	public KTAmbulance(int sp, int ep,double muCall, double muServe,
			double alpha, double delta,double[][] altVec, int seed, 
			int n0Prime) {
		this.sp = sp;
		this.ep = ep;
		this.muCall = muCall;
		this.muServe = muServe;
		this.alpha = alpha;
		this.delta = delta;
		this.altVec = altVec;
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
	
	public void run() {
		R.setSeed((long)seed);
		ArrayList<Integer> I = new ArrayList<Integer>();
		int k = altVec.length;
		
		int round = 1;
		for(int i = 0 ; i < k; i++) {
				I.add(i);
		}
		while(I.size()>1) {
			ArrayList<Integer> tempI = new ArrayList<Integer>();
			double alpha_r = alpha/Math.pow(2, round);
			
			
			
			
			
			int numOfMatches=(int)Math.floor(I.size()*1.0/2.0); 
			if(I.size()*1.0/2.0>(int)Math.floor(I.size()*1.0/2.0)) {
				tempI.add(I.get(I.size()-1));
				I.remove(I.size()-1);
			}
			
			totalNumberOfMatches = totalNumberOfMatches+numOfMatches;
			//System.out.println(I.size()+" "+numOfMatches);
			for(int i = 0;  i < numOfMatches; i++) {
	
				double[][] groupAltVec = new double[2][9];
				ArrayList<Integer> group = new ArrayList<Integer>();
					
					
				for(int j = 0; j < 2; j++) {
					int sample=(int)Math.floor(R.nextDouble()*I.size());
						
					group.add(I.get(sample));
					for(int ell=0; ell < 9; ell++) {
						groupAltVec[j][ell] =altVec[I.get(sample)][ell];
					}
						
					I.remove(sample);
				}
				KNAmbulance y = new KNAmbulance(sp,ep,muCall,muServe,alpha_r,delta,groupAltVec,R.nextInt(),n0Prime);

				y.run();
					
				sampleSize = sampleSize + y.getSampleSize();
				//y.getSampleSizeWithoutRef();
			//System.out.println(y.getBestID()+" "+group.size());
				tempI.add(group.get(y.getBestID()));
						
				
			}
			I.addAll(tempI);
			round++;
			
		}
		bestID=I.get(0);
	}
}
