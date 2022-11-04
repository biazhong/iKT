//This is the KN procedure which assumes that the variances of the alternatives are known.

package section6_1;

import java.util.ArrayList;
import java.util.Random;
public class KNKnownVariance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	ArrayList<Double> mu = new ArrayList<Double>();
	ArrayList<Double> sigma2 = new ArrayList<Double>();
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	
	ArrayList<Double> refSamples = new ArrayList<Double>();
	Random R = new Random();
	
	public KNKnownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
	}
	public KNKnownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed) {
		this.alpha = alpha;
		this.delta = delta;
		this.mu.addAll(mu);
		this.sigma2.addAll(sigma2);
		this.seed = seed;
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
	
	//While conducting the selection, the reference alternative is the 1st alternative in set I.
	public void run() {
		R.setSeed((long)seed);
		ArrayList<Integer> I = new ArrayList<Integer>();
		int k = mu.size();
		for(int i = 0 ; i < k; i++) {
			I.add(i);
		}
		double[] X = new double[I.size()];
		
		double a = Math.log((k-1.0)/(2*alpha))/delta;
		
		int t = 0;
		while(I.size()>1) {
			for(int i = 0 ; i < I.size(); i++) {
				if(I.get(i)==0) {
					if(refSamples.size()>t) {
						X[I.get(i)] = X[I.get(i)] + refSamples.get(t);
					}else {
						refSamples.add(R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i)));
						X[I.get(i)] = X[I.get(i)] + refSamples.get(t);
					}
				}else {
					X[I.get(i)] = X[I.get(i)] + R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i));
				}	
			}
			t++;
			int standard = 0;
			
			for(int i = 1; i < I.size();i++) {
				if(X[I.get(i)]>X[I.get(0)]) {
					standard = i;
				}
			}
			int temp = I.get(0);
			I.set(0, I.get(standard));
			I.set(standard, temp);
			boolean checkEli = false;
			for(int j = 1; j <I.size();j++) {
				double boundary = a - delta*t/(2*(sigma2.get(I.get(0))+sigma2.get(I.get(j))));
				double Zij = (X[I.get(0)]-X[I.get(j)])/(sigma2.get(I.get(0))+sigma2.get(I.get(j)));
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
				I.remove(0);
			}
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
