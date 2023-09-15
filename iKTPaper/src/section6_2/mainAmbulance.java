//This is the main class that implements the first experiment in Section 6.2


package section6_2;


import java.util.Random;

import org.apache.commons.math3.distribution.TDistribution;
public class mainAmbulance {
	public static void main(String[] args) {
		double alpha = 0.05;	//PICS
		double delta = 0.001;		//Delta
		int repeat = 100;		//Number of macro replications.
		int correctness = 0;	//Count the number of correct selections 
		int n0 = 20;	//Initial stage sample size for iKT.
		int n0Prime = 20; //First stage sample size of KN when variances are unknown
		int seed =  533145; //Pseudo random number seed used to conduct the experiment.
		int sp = 250;
		int ep = 500;
		double muCall = 55d/60;
		double muServe = 10d/60;
		double[][] altVec = new double[495][9];
		int numOfAlt = 0;
		for(int i1 = 0 ; i1 < 6; i1++) {
			for(int i2 = i1+1; i2 < 7; i2++) {
				for(int i3 = i2 + 1; i3 < 8; i3++) {
					for(int i4 = i3 + 1; i4 < 9; i4++) {
						altVec[numOfAlt][i1]=1;
						altVec[numOfAlt][i2]=1;
						altVec[numOfAlt][i3]=1;
						altVec[numOfAlt][i4]=1;
						numOfAlt++;
					}
				}
			}
		}
		
		for(int i1 = 0 ; i1 < 8; i1++) {
			for(int i2 = i1+1; i2 < 9; i2++) {
				altVec[numOfAlt][i1]=2;
				altVec[numOfAlt][i2]=2;
				numOfAlt++;
			}
		}

		for(int i1 = 0; i1 < 9; i1++) {
			for(int i2 = 0; i2 < 8; i2++) {
				for(int i3 = i2+1; i3 < 9;i3++) {
					if(i2!=i1 && i3!=i1) {
						altVec[numOfAlt][i1]=2;
						altVec[numOfAlt][i2]=1;
						altVec[numOfAlt][i3]=1;
						numOfAlt++;
					}
				}
			}
		}

		for(int i1 = 0 ; i1 < 9;i1++) {
			for(int i2 = 0; i2 < 9; i2++) {
				if(i2!=i1) {
					altVec[numOfAlt][i1]=3;
					altVec[numOfAlt][i2]=1;
					numOfAlt++;
				}
			}
		}
		for(int i1 = 0 ; i1 < 9;i1++) {
			altVec[numOfAlt][i1]=4;
			numOfAlt++;
		}
		
		Random R = new Random((long)seed);
		
		
		for(int count = 0; count<repeat; count++) {
			int seedInOneReplication = R.nextInt();
			//KNAmbulance y = new KNAmbulance(sp,ep,muCall,muServe,alpha,delta,altVec,seedInOneReplication,n0Prime);
			//iKTAmbulance y = new iKTAmbulance(sp,ep,muCall,muServe,alpha,delta,altVec,seedInOneReplication,n0Prime,n0);
			//KTAmbulance y = new KTAmbulance(sp,ep,muCall,muServe,alpha,delta,altVec,seedInOneReplication,n0Prime);
			//iKTPGSAmbulance y = new iKTPGSAmbulance(sp,ep,muCall,muServe,alpha,delta,altVec,seedInOneReplication,n0Prime,n0);
			KTSAmbulance y = new KTSAmbulance(sp,ep,muCall,muServe,alpha,delta,altVec,seedInOneReplication,n0Prime);
			
			long _start = System.nanoTime();
			y.run();
			int selected = y.getBestID();
			double run_time = (System.nanoTime()-_start)/1e9;
			System.out.println(count+" "+selected+" "+y.getSampleSize()+" "+run_time+" "+altVec[selected][0]+" "+altVec[selected][1]
					+" "+altVec[selected][2]+" "+altVec[selected][3]+" "
					+altVec[selected][4]+" "+altVec[selected][5]+" "
					+altVec[selected][6]+" "+altVec[selected][7]+" "
					+altVec[selected][8]);
		}
		
		
	}
}
