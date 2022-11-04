package SparkProgramming.iKTPaperSpark;

import java.util.ArrayList;
import java.util.Random;



public class iKTTruck {
	double alpha,delta;
	int bestID=-1,sampleSize=0;
	int seed = 1374;
	double bestAVG = 0d;
	int bestSampleSize = 0;
	int refSampleSize = 0;
	int n0,n0Prime;
	double muCall, muServe1, muServe2;
	int totalNumberOfMatches = 0;
	double simulationTime = 0d;
	int maxRound = 1;
	int coreID;
	int nCores;
	
	
	Random R = new Random();
	
	public iKTTruck() {
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
	public iKTTruck(double muCall, double muServe1, double muServe2,
			double alpha, double delta,int coreID, int nCores, int seed, 
			int n0Prime, int n0) {

		this.muCall = muCall;
		this.muServe1 = muServe1;
		this.muServe2 = muServe2;
		this.alpha = alpha;
		this.delta = delta;
		this.coreID = coreID;
		this.nCores = nCores;
		this.seed = seed;
		this.n0Prime  = n0Prime;
		this.n0 = n0;
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
		double[] frakX = new double[k];
		int[] T = new int[k];
		double _s = System.nanoTime();
		System.nanoTime();
		for(int i = 0 ; i < k; i++) {
			I.add(i);
			for(int j = 0 ; j < n0; j ++) {
				for(int ell=0; ell < 7; ell++) {
					tempAlt[ell] =altVec[i][ell];
				}
				genObv.setAlt(tempAlt);
				genObv.run();
				frakX[i]=frakX[i]+genObv.getAVGWaitingTime();
			}
			T[i]=n0;
		}
		simulationTime = simulationTime + System.nanoTime()-_s;
		sampleSize = sampleSize + n0*k;
		int round = 1;
		//double compareT =0d;
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
			//	if(coreID==0) {
				//	System.out.println(numOfMatches+"match");
				//}
				if(coreID ==0) {
					//System.out.println("match"+i);
				}
				if(i < numOfMatches - 1) {
					//if(coreID==0) {
						//System.out.println("frakRef"+frakX[refAlt]/T[refAlt]);
					//}
					double[][] groupAltVec = new double[3][7];
					ArrayList<Integer> group = new ArrayList<Integer>();
					group.add(refAlt);
					for(int ell=0; ell < 7; ell++) {
						groupAltVec[0][ell] =altVec[refAlt][ell];
					}
					
					for(int j = 0; j < 2; j++) {
						int sample=0;
						
						group.add(I.get(sample));
						for(int ell=0; ell < 7; ell++) {
							groupAltVec[j+1][ell] =altVec[I.get(sample)][ell];
						}
						
						I.remove(sample);
					}
				//	double _ss = System.nanoTime();
					KNTruck y = new KNTruck(muCall,muServe1,muServe2,alpha_r,delta,groupAltVec,R.nextInt(),n0Prime);
					y.setRefSamples(refSamples);
					y.run();
					
					//compareT = compareT + System.nanoTime()-_ss;
					simulationTime=simulationTime+y.getSimulationTime();
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
					int gPrime = I.size();
		
					double[][] groupAltVec = new double[gPrime+1][7];
					group.add(refAlt);
					for(int ell=0; ell < 7; ell++) {
						groupAltVec[0][ell] =altVec[refAlt][ell];
					}
					int _c=1;
					
					for(int j = 0; j < gPrime; j++) {
							int sample = 0; 
							group.add(I.get(sample));
							for(int ell=0; ell < 7; ell++) {
								groupAltVec[_c][ell] =altVec[I.get(sample)][ell];
							}
							_c++;
							I.remove(sample);
							
					}
						
					KNTruck y = new KNTruck(muCall,muServe1,muServe2,alpha_r,delta,groupAltVec,R.nextInt(),n0Prime);
					y.setRefSamples(refSamples);
					y.run();
					simulationTime=simulationTime+y.getSimulationTime();
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
			
			maxRound = round;
			round++;

			
			
			sampleSize = sampleSize + refSamples.size();
		}
		//System.out.println("sadfasdfasdfsad"+(I.get(0)*nCores+coreID));
		bestID=I.get(0)*nCores+coreID;
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