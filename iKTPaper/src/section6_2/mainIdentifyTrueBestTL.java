//This main class is designed to identify the true best alternative in the truck allocation problem.

package section6_2;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class mainIdentifyTrueBestTL {
	public static void main(String[] args) throws FileNotFoundException{
		int repeat = 10000;		//Number of macro replications.
		int seed =   633232; //Pseudo random number seed used to conduct the experiment.
		double muCall = 6d/60;
		double muServe1 = 3d/60;
		double muServe2 = 2d/60;
		
		double[][] altVec = new double[12376][7];
		int numOfAlt = 0;
		for(int i1 =0;i1<=11;i1++) {
			for(int i2 =i1; i2 <=11;i2++) {
				for(int i3 =i2; i3 <=11;i3++) {
					for(int i4 =i3; i4 <=11;i4++) {
						for(int i5 =i4; i5 <=11;i5++) {
							for(int i6 = i5; i6<=11;i6++) {
								altVec[numOfAlt][0]=i1;
								altVec[numOfAlt][1]=i2-i1;
								altVec[numOfAlt][2]=i3-i2;
								altVec[numOfAlt][3]=i4-i3;
								altVec[numOfAlt][4]=i5-i4;
								altVec[numOfAlt][5]=i6-i5;
								altVec[numOfAlt][6]=11-i6;
								numOfAlt++;
							}
						}
					}
				}
			}
		}
		

		
		double[] recordAVG = new double[numOfAlt];
		Random R = new Random((long)seed);
		for(int count = 0; count<repeat; count++) {
			double[] tempAlt = new double[7];
			genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,R.nextInt());
			numOfAlt =1;
			for(int s=0; s < numOfAlt; s++) {
				for(int ell=0; ell < 7; ell++) {
					tempAlt[ell] =altVec[s][ell];
				}
				genObv.setAlt(tempAlt);
				genObv.run();
				recordAVG[s]=recordAVG[s]+genObv.getAVGWaitingTime();
			}
			System.out.println(count);
		}
		String outputString ="";

		for(int s = 0;s <numOfAlt;s++) {
			recordAVG[s] = recordAVG[s]/repeat;
			outputString=outputString+s+" "+altVec[s][0]+" "+altVec[s][1]+" "
				+altVec[s][2]+" "+altVec[s][3]+" "+altVec[s][4]+" "
				+altVec[s][5]+" "+altVec[s][6]+" "+recordAVG[s]+"\n";
			System.out.println(s+" "+altVec[s][0]+" "+altVec[s][1]+" "
					+altVec[s][2]+" "+altVec[s][3]+" "+altVec[s][4]+" "
					+altVec[s][5]+" "+altVec[s][6]+" "+recordAVG[s]);
		}
		PrintWriter out = new PrintWriter("C:\\Users\\Ying\\Desktop\\Output.txt");
		out.println(outputString);

	}
}
