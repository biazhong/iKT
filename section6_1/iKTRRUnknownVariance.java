package section6_1;

import java.util.ArrayList;
import java.util.Random;

public class iKTRRUnknownVariance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	ArrayList<Double> mu = new ArrayList<Double>();
	ArrayList<Double> sigma2 = new ArrayList<Double>();
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int n0Prime;
	int seed = 1374;
	Random R = new Random();
	int totalNumberOfMatches = 0;
	
	public iKTRRUnknownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
		n0Prime=10;
	}
	public iKTRRUnknownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed,int n0Prime) {
		this.alpha = alpha;
		this.delta = delta;
		this.mu.addAll(mu);
		this.sigma2.addAll(sigma2);
		this.seed = seed;
		this.n0Prime = n0Prime;
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
		int k = mu.size();
		for(int i = 0 ; i < k; i++) {
			I.add(i);
		}
		int round = 1;

		while(I.size()>1) {
			ArrayList<Integer> tempI = new ArrayList<Integer>();
			ArrayList<Double> refSamples = new ArrayList<Double>();
			double alpha_r = alpha/Math.pow(2, round);
			
			
			int refAlt = I.get(I.size()-1);
			I.remove(I.size()-1);
			
			
			int numOfMatches = (int)Math.ceil(I.size()*1.0/2.0);
			
			totalNumberOfMatches = totalNumberOfMatches+numOfMatches;
			
			for(int i = 0;  i < numOfMatches; i++) {
				if(i < numOfMatches - 1) {
					ArrayList<Integer> group = new ArrayList<Integer>();
					ArrayList<Double> groupMu = new ArrayList<Double>();
					ArrayList<Double> groupSigma2 = new ArrayList<Double>();
					group.add(refAlt);
					groupMu.add(mu.get(refAlt));
					groupSigma2.add(sigma2.get(refAlt));
					for(int j = 0; j < 2; j++) {
						int sample=0;
						
						group.add(I.get(sample));
						groupMu.add(mu.get(I.get(sample)));
						groupSigma2.add(sigma2.get(I.get(sample)));

						I.remove(sample);
					}
					KNUnknownVariance y = new KNUnknownVariance(alpha_r, delta, groupMu, groupSigma2,R.nextInt(),n0Prime);
					y.setRefSamples(refSamples);
					y.run();
					refSamples.clear();
					refSamples.addAll(y.getRefSamples());
					
					sampleSize = sampleSize + y.getSampleSizeWithoutRef();
					
					if(i==0) {
						tempI.add(group.get(y.getBestID()));
					}else {
						if(y.getBestID()!=0) {
							tempI.add(group.get(y.getBestID()));
						}
					}
				}else {
					ArrayList<Integer> group = new ArrayList<Integer>();
					ArrayList<Double> groupMu = new ArrayList<Double>();
					ArrayList<Double> groupSigma2 = new ArrayList<Double>();
					group.add(refAlt);
					groupMu.add(mu.get(refAlt));
					groupSigma2.add(sigma2.get(refAlt));
					
					int gPrime = I.size();
					
					for(int j = 0; j < gPrime; j++) {
							int sample = 0; 
							group.add(I.get(sample));
							groupMu.add(mu.get(I.get(sample)));
							groupSigma2.add(sigma2.get(I.get(sample)));
							
							I.remove(sample);
							
					}
						
					KNUnknownVariance y = new KNUnknownVariance(alpha_r, delta, groupMu, groupSigma2,R.nextInt(),n0Prime);
					y.setRefSamples(refSamples);
					y.run();
					refSamples.clear();
					refSamples.addAll(y.getRefSamples());
					sampleSize = sampleSize + y.getSampleSizeWithoutRef();
					if(i==0) {
						tempI.add(group.get(y.getBestID()));
						
					}else {
						if(y.getBestID()!=0) {
							tempI.add(group.get(y.getBestID()));
							
						}
					}
				}
			}
			I.addAll(tempI);
			round++;
			sampleSize = sampleSize + refSamples.size();
			
		}
		bestID=I.get(0);
	}
	
	
}
