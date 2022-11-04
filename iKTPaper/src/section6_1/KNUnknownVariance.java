package section6_1;

import java.util.ArrayList;
import java.util.Random;

public class KNUnknownVariance {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	ArrayList<Double> mu = new ArrayList<Double>();
	ArrayList<Double> sigma2 = new ArrayList<Double>();
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	int n0Prime;
	
	ArrayList<Double> refSamples = new ArrayList<Double>();
	Random R = new Random();
	
	public KNUnknownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
		n0Prime = 10;
	}
	public KNUnknownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed, int n0Prime) {
		this.alpha = alpha;
		this.delta = delta;
		this.mu.addAll(mu);
		this.sigma2.addAll(sigma2);
		this.seed = seed;
		this.n0Prime  = n0Prime;
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
						refSamples.add(R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i)));
						X[i] = X[i] + refSamples.get(t);
						tempX[i][t]=refSamples.get(t);
					}
				}else {
					tempX[i][t]=R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i));
					X[i] = X[i] + tempX[i][t];
					
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
						refSamples.add(R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i)));
						X[I.get(i)] = X[I.get(i)] + refSamples.get(t);
					}
				}else {
					X[I.get(i)] = X[I.get(i)] + R.nextGaussian()*Math.sqrt(sigma2.get(I.get(i)))+mu.get(I.get(i));
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
