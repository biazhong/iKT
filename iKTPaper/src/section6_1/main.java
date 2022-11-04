//This is the main class that implements  experiments in Section 6.1

package section6_1;
import java.util.ArrayList;
import java.util.Random;

public class main {
	public static void main(String[] args) {
		int k = 100000;			//Number of alternatives
		double alpha = 0.05;	//PICS
		double delta = 0.1;		//Delta
		int repeat = 1000;		//Number of macro replications.
		int correctness = 0;	//Count the number of correct selections 
		int n0 = 20;	//Initial stage sample size for iKT.
		int n0Prime = 20; //First stage sample size of KN when variances are unknown
		int seed =   741587; //Pseudo random number seed used to conduct the experiment.
		
		double recordSampleSize = 0;
		Random R = new Random((long)seed);
		for(int count = 0;  count < repeat; count++) {
		
			
	
			//Generate mean and variance configurations
			ArrayList<Double> mu = new ArrayList<Double>();
			ArrayList<Double> sigma2 = new ArrayList<Double>();
			for(int i = 0; i < k; i++) {
				if(i==5) {
					mu.add(delta);
					//mu.add(17*delta);
					sigma2.add(1d);
					//sigma2.add(R.nextDouble()+0.5);
				}else {
					mu.add(0d);
					//mu.add(16*delta*R.nextDouble());
					sigma2.add(1d);
					//sigma2.add(R.nextDouble()+0.5);
				}
			}
			
			//Find best alt
			int best = -1;
			double bestMean = -100000;
			for(int i = 0; i < k; i++) {
				if(mu.get(i)>bestMean) {
					best = i;
					bestMean = mu.get(i);
				}
				
			}
			int seedInOneReplication = R.nextInt();
			//Run procedures
			//KNKnownVariance y = new KNKnownVariance(alpha, delta, mu, sigma2,seedInOneReplication);
			//KTKnownVariance y = new KTKnownVariance(alpha, delta, mu, sigma2,seedInOneReplication);
			iKTKnownVariance y = new iKTKnownVariance(alpha, delta, mu, sigma2,seedInOneReplication,n0);
			//iKTUnknownVariance y = new iKTUnknownVariance(alpha, delta, mu, sigma2,seedInOneReplication,n0,n0Prime);
			//iKTRRUnknownVariance y = new iKTRRUnknownVariance(alpha, delta, mu, sigma2,seedInOneReplication,n0Prime);
			
			y.run();
			
			//Record results
			recordSampleSize = recordSampleSize + y.getSampleSize();
			if(y.getBestID()==best) {
				correctness++;
			}
			System.out.println("Replication: "+count+" "+y.getBestID()+" "+y.getSampleSize());
			
			//System.out.println("Replication: "+count+" "+y.getBestID()+" "+y.getSampleSize()+" "+y.getTotalMatches());
		}
	}
}