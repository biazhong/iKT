package section6_1;

import java.util.ArrayList;
import java.util.Random;

public class iKTUnknownVariance {
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
	
	public iKTUnknownVariance() {
		alpha = 0.05;
		delta = 0.1;
		mu.add(delta);
		mu.add(0d);
		sigma2.add(1d);
		sigma2.add(1d);
		seed = 123;
		n0=10;
	}
	public iKTUnknownVariance(double alpha, double delta, ArrayList<Double> mu, ArrayList<Double> sigma2, int seed,int n0,int n0Prime) {
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
	
	public void run() {
		R.setSeed((long)seed);
		ArrayList<Integer> I = new ArrayList<Integer>();
		int k = mu.size();
		double[] frakX = new double[k];
		int[] T = new int[k];
		for(int i = 0 ; i < k; i++) {
			I.add(i);
			frakX[i]=R.nextGaussian()*Math.sqrt(sigma2.get(i)*n0)+n0*mu.get(i);
			T[i]=n0;
		}
		sampleSize = sampleSize + n0*k;
		int round = 1;
		
		while(I.size()>1) {
			ArrayList<Integer> tempI = new ArrayList<Integer>();
			ArrayList<Double> refSamples = new ArrayList<Double>();
			double alpha_r = alpha/Math.pow(2, round);
			
			double[] Array = new double[I.size()];
			
			for(int i = 0; i < I.size(); i++) {
				Array[i]=frakX[I.get(i)]/T[I.get(i)];
				
			}
			mergeSort(Array,I,0,I.size()-1);
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
						frakX[group.get(y.getBestID())]=frakX[group.get(y.getBestID())]+y.getBestAVG()*y.getBestSampleSize();
						T[group.get(y.getBestID())]=T[group.get(y.getBestID())]+y.getBestSampleSize();
					}else {
						if(y.getBestID()!=0) {
							tempI.add(group.get(y.getBestID()));
							frakX[group.get(y.getBestID())]=frakX[group.get(y.getBestID())]+y.getBestAVG()*y.getBestSampleSize();
							T[group.get(y.getBestID())]=T[group.get(y.getBestID())]+y.getBestSampleSize();
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
						frakX[group.get(y.getBestID())]=frakX[group.get(y.getBestID())]+y.getBestAVG()*y.getBestSampleSize();
						T[group.get(y.getBestID())]=T[group.get(y.getBestID())]+y.getBestSampleSize();
					
					}else {
						if(y.getBestID()!=0) {
							tempI.add(group.get(y.getBestID()));
							frakX[group.get(y.getBestID())]=frakX[group.get(y.getBestID())]+y.getBestAVG()*y.getBestSampleSize();
							T[group.get(y.getBestID())]=T[group.get(y.getBestID())]+y.getBestSampleSize();
							
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
	
	public static void mergeSort(double[] array, ArrayList<Integer> I, int left, int right) {
	    if (right <= left) return;
	    int mid = (left+right)/2;
	    mergeSort(array, I ,left, mid);
	    mergeSort(array, I ,mid+1, right);
	    merge(array, I ,left, mid, right);
	}

	public static void merge(double[] array, ArrayList<Integer> I,int left, int mid, int right) {
	    // calculating lengths
	    int lengthLeft = mid - left + 1;
	    int lengthRight = right - mid;
	    
	    

	    // creating temporary subarrays
	    double leftArray[] = new double [lengthLeft];
	    double rightArray[] = new double [lengthRight];

	    ArrayList<Integer> leftI = new ArrayList<Integer>();
	    ArrayList<Integer> rightI = new ArrayList<Integer>();
	    
	    // copying our sorted subarrays into temporaries
	    for (int i = 0; i < lengthLeft; i++) {
	    	leftArray[i] = array[left+i];
	    	leftI.add(I.get(left+i));
	    }
	        
	    for (int i = 0; i < lengthRight; i++) {
	        rightArray[i] = array[mid+i+1];
	        rightI.add(I.get(mid+i+1));
	    }

	    // iterators containing current index of temp subarrays
	    int leftIndex = 0;
	    int rightIndex = 0;

	    // copying from leftArray and rightArray back into array
	    for (int i = left; i < right + 1; i++) {
	        // if there are still uncopied elements in R and L, copy minimum of the two
	        if (leftIndex < lengthLeft && rightIndex < lengthRight) {
	            if (leftArray[leftIndex] < rightArray[rightIndex]) {
	                array[i] = leftArray[leftIndex];
	                I.set(i, leftI.get(leftIndex));
	                leftIndex++;
	            }
	            else {
	                array[i] = rightArray[rightIndex];
	                I.set(i, rightI.get(rightIndex));
	                rightIndex++;
	            }
	        }
	        // if all the elements have been copied from rightArray, copy the rest of leftArray
	        else if (leftIndex < lengthLeft) {
	            array[i] = leftArray[leftIndex];
	            I.set(i, leftI.get(leftIndex));
	            leftIndex++;
	        }
	        // if all the elements have been copied from leftArray, copy the rest of rightArray
	        else if (rightIndex < lengthRight) {
	            array[i] = rightArray[rightIndex];
	            I.set(i, rightI.get(rightIndex));
	            rightIndex++;
	        }
	    }
	}
}
