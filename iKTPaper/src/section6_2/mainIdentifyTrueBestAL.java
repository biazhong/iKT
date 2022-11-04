//This main class is designed to identify the true best alternative in the ambulance allocation problem.
package section6_2;

import java.util.Random;

public class mainIdentifyTrueBestAL {
	public static void main(String[] args){
		int repeat = 100000;		//Number of macro replications.
		int seed =   243532; //Pseudo random number seed used to conduct the experiment.
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
		
		double[] recordAVG = new double[495];
		Random R = new Random((long)seed);
		for(int count = 0; count<repeat; count++) {
			double[] tempAlt = new double[9];
			genAmbulanceObv genObv = new genAmbulanceObv(sp,ep,muCall,muServe,R.nextInt());
			
			for(int s=0; s < 495; s++) {
				for(int ell=0; ell < 9; ell++) {
					tempAlt[ell] =altVec[s][ell];
				}
				genObv.setAlt(tempAlt);
				genObv.run();
				recordAVG[s]=recordAVG[s]+genObv.getPerOfGC();
			}
			System.out.println(count);
		}
		for(int s = 0;s <495;s++) {
			recordAVG[s] = recordAVG[s]/repeat;
			System.out.println(s+" "+altVec[s][0]+" "+altVec[s][1]+" "
				+altVec[s][2]+" "+altVec[s][3]+" "+altVec[s][4]+" "
				+altVec[s][5]+" "+altVec[s][6]+" "+altVec[s][7]+" "
				+altVec[s][8]+" "+recordAVG[s]);
		}
	}
}
