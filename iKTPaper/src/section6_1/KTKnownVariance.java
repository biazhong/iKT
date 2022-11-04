package section6_1;

import java.util.ArrayList;
import java.util.Random;


public class KTKnownVariance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	ArrayList<Double> mu = new ArrayList<Double>();
	ArrayList<Double> sigma2 = new ArrayList<Double>();
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int seed = 1374;
	
	Random R = new Random();
	
	public KTKnownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
	}
	public KTKnownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed) {
		this.alpha = alpha;
		this.delta = delta;
		this.mu.addAll(mu);
		this.sigma2.addAll(sigma2);
		this.seed = seed;
	}
	public int getBestID() {
		return bestID;
	}
	
	public int getSampleSize() {
		return sampleSize;
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
			double alpha_r = alpha/Math.pow(2, round);
			
			int numOfMatches = (int)Math.ceil(I.size()*1.0/2.0);
			if(I.size()*1.0/2.0<numOfMatches) {
				tempI.add(I.get(I.size()-1));
				I.remove(I.size()-1);
				numOfMatches=numOfMatches-1;
			}
			
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
				KNKnownVariance y = new KNKnownVariance(alpha_r, delta, groupMu, groupSigma2,R.nextInt());
				y.run();
				sampleSize = sampleSize + y.getSampleSize();
				tempI.add(group.get(y.getBestID()));
			}
			I.addAll(tempI);
			round++;
		}
		bestID=I.get(0);
	}
}
