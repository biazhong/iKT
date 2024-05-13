package org.example;

//This main class is designed to identify the true best alternative in the truck allocation problem.


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class mainIdentifyTrueBestTL {
    public static void main(String[] args) throws FileNotFoundException{
        int repeat = 100000;		//Number of macro replications.
        int seed =   634532; //Pseudo random number seed used to conduct the experiment.
        double muCall = 4d/60;
        double muServe1 = 2d/60;
        double muServe2 = 2d/60;
        int numOfTruck = 15;
        int idAlt = 44685;


        double[][] altVec = new double[54264][7];
        int numOfAlt = 0;
        for(int i1 =0;i1<=numOfTruck;i1++) {
            for(int i2 =i1; i2 <=numOfTruck;i2++) {
                for(int i3 =i2; i3 <=numOfTruck;i3++) {
                    for(int i4 =i3; i4 <=numOfTruck;i4++) {
                        for(int i5 =i4; i5 <=numOfTruck;i5++) {
                            for(int i6 = i5; i6<=numOfTruck;i6++) {
                                altVec[numOfAlt][0]=i1;
                                altVec[numOfAlt][1]=i2-i1;
                                altVec[numOfAlt][2]=i3-i2;
                                altVec[numOfAlt][3]=i4-i3;
                                altVec[numOfAlt][4]=i5-i4;
                                altVec[numOfAlt][5]=i6-i5;
                                altVec[numOfAlt][6]=numOfTruck-i6;
                                numOfAlt++;
                            }
                        }
                    }
                }
            }
        }
        System.out.println(numOfAlt);
        long __s = System.nanoTime();
        double[] recordAVG = new double[numOfAlt];

        Random R = new Random((long)seed);
        for(int count = 0; count<repeat; count++) {
            double[] tempAlt = new double[7];
            genTruckObv genObv = new genTruckObv(muCall,muServe1,muServe2,R.nextInt());

            for(int s=0; s < numOfAlt; s++) {

                if(s == idAlt) {


                    for (int ell = 0; ell < 7; ell++) {
                        tempAlt[ell] = altVec[s][ell];
                    }
                    genObv.setAlt(tempAlt);
                    genObv.run();
                    recordAVG[s] = recordAVG[s] + genObv.getAVGWaitingTime();
                }
            }
                System.out.println(count);

        }
        String outputString ="";
        System.out.println((System.nanoTime()-__s)/1e9);
        System.out.println(numOfAlt);
        for(int s = 0;s <numOfAlt;s++) {
            if(s == idAlt) {
                recordAVG[s] = recordAVG[s] / repeat;
                outputString = outputString + s + " " + altVec[s][0] + " " + altVec[s][1] + " "
                        + altVec[s][2] + " " + altVec[s][3] + " " + altVec[s][4] + " "
                        + altVec[s][5] + " " + altVec[s][6] + " " + recordAVG[s] + "\n";
                System.out.println(s + " " + altVec[s][0] + " " + altVec[s][1] + " "
                        + altVec[s][2] + " " + altVec[s][3] + " " + altVec[s][4] + " "
                        + altVec[s][5] + " " + altVec[s][6] + " " + recordAVG[s]);
            }
        }

        PrintWriter out = new PrintWriter("/home/ying/Desktop/Output.txt");
        out.println(outputString);
        out.close();
    }
}
