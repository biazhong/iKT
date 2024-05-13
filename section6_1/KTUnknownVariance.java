package section6_1;

import java.util.ArrayList;
import java.util.Random;

public class KTUnknownVariance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	ArrayList<Double> mu = new ArrayList<Double>();
	ArrayList<Double> sigma2 = new ArrayList<Double>();
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int n0,n0Prime;
	int seed = 1374;
	Random R = new Random();
	int totalNumberOfMatches = 0;
	int maxRound = -1;
	int roundToSelectRef = -1;
	
	public KTUnknownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
		n0=10;
	}
	public KTUnknownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed,int n0,int n0Prime) {
		this.alpha = alpha;
		this.delta = delta;
		this.mu.addAll(mu);
		this.sigma2.addAll(sigma2);
		this.seed = seed;
		this.n0 = n0;
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
	
	public int getMaxRound() {
		return maxRound;
	}
	
	public int getRoundToSelectRef() {
		return roundToSelectRef;
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
			
			
			
			
			
			int numOfMatches = (int)Math.ceil(I.size()*1.0/2.0);
			if(I.size()*1.0/2.0<numOfMatches) {
				tempI.add(I.get(0));
				I.remove(0);
				numOfMatches = numOfMatches-1;
			}
			
			totalNumberOfMatches = totalNumberOfMatches+numOfMatches;
			
			for(int i = 0;  i < numOfMatches; i++) {
				
					ArrayList<Integer> group = new ArrayList<Integer>();
					ArrayList<Double> groupMu = new ArrayList<Double>();
					ArrayList<Double> groupSigma2 = new ArrayList<Double>();
					
					for(int j = 0; j < 2; j++) {
						int sample = (int)Math.floor(R.nextDouble()*I.size());
						group.add(I.get(sample));
						groupMu.add(mu.get(I.get(sample)));
						groupSigma2.add(sigma2.get(I.get(sample)));
							
						I.remove(sample);
					}
					KNUnknownVariance y = new KNUnknownVariance(alpha_r, delta, groupMu, groupSigma2,R.nextInt(),n0Prime);
					y.run();
					sampleSize = sampleSize + y.getSampleSize() ;
					tempI.add(group.get(y.getBestID()));
			}
			
			I.addAll(tempI);
			maxRound = round;
			round++;
			sampleSize = sampleSize + refSamples.size();
		}
		bestID=I.get(0);
	}
	
	
}
