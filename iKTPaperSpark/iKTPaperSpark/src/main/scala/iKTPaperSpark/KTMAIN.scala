package iKTPaperSpark
import scala.util.Random;
import org.apache.spark._;
import org.apache.spark.SparkContext._;
import SparkProgramming.iKTPaperSpark._;
import java.io._;
object KTMAIN {
  def withinCoreSelection( muCall:Double,muServe1:Double,muServe2:Double,alpha:Double,delta:Double
      ,CoreID:Int,nCores:Int,seed:Int,n0Prime:Int,acc_sim_t: Accumulator[Double],
      acc_sample_size: Accumulator[Double]):(Int,Int)={
    val KT = new KTTruck(muCall,muServe1,muServe2,alpha,delta,CoreID,nCores,seed+CoreID,n0Prime);
    KT.run();
    acc_sim_t += KT.getSimulationTime();
    acc_sample_size +=KT.getSampleSize;
    
    (KT.getBestID(),KT.getMaxRound);
  }
  
  def finalSelection(muCall:Double,muServe1:Double,muServe2:Double,alpha:Double,delta:Double
      ,altID:Array[Int],coreID:Int,maxRound:Int,n0Prime:Int,nCores:Int,seed:Int,acc_sim_t: Accumulator[Double],
      acc_sample_size: Accumulator[Double]):(Int,Double)={
      
    val rinottSelection = new RinottSelection(muCall, muServe1,muServe2,alpha,delta,nCores,maxRound,
          altID(coreID), seed+coreID,n0Prime);
      rinottSelection.run();
      acc_sim_t += rinottSelection.getSimulationTime();
      acc_sample_size +=rinottSelection.getSampleSize;
      (altID(coreID),rinottSelection.getSampleAVG());
  }
  
  def getElementFromArray(array1:Array[Int],s:Int):(Int)={
    (array1(s));
  }
  
  
  def main(args:Array[String]):Unit={
    val muCall = 0.1;
    val muServe1 = 0.05;
    val muServe2 =1d/30;
    val alpha = 0.05;
    val delta = 1d/60;
    val n0Prime = 20;
    val nCores = args(0).toInt;
    val repeat = args(1).toInt;
    val seed = args(2).toInt;
    val conf = new SparkConf().setAppName("KT-Spark").set("spark.cores.max",nCores.toString());
    val sc = new SparkContext(conf);
    val R = new Random(seed);
    var count =0;  
    var outputstring="";
    
    var altVec =  Array.ofDim[Int](12376,7);
    
		var numOfAlt = 0;
		for(i1 <- 0 to 11) {
			for(i2 <- i1 to 11) {
				for(i3 <- i2 to 11) {
					for(i4 <- i3 to 11) {
						for(i5 <- i4 to 11) {
							for(i6 <- i5 to 11) {
								altVec(numOfAlt)(0)=i1;
								altVec(numOfAlt)(1)=i2-i1;
								altVec(numOfAlt)(2)=i3-i2;
								altVec(numOfAlt)(3)=i4-i3;
								altVec(numOfAlt)(4)=i5-i4;
								altVec(numOfAlt)(5)=i6-i5;
								altVec(numOfAlt)(6)=11-i6;
								
								numOfAlt+=1;
							}
						}
					}
				}
			}
		}
    
    
    
    
    while (count < repeat){
      val start_t = System.nanoTime();
      val coreID = sc.parallelize(0 to nCores-1,nCores);
    
    
      val accum_sim_t = sc.accumulator(0D,"Accumulator: total simulation time");
      val accum_sample_size = sc.accumulator(0D,"Accumulator: total sample size");

    
      val initialSeed= Math.ceil(R.nextDouble()*10000);
      val seedFinal = Math.ceil(R.nextDouble()*10000);
    
      val coreOutPut = coreID.map(withinCoreSelection(muCall,muServe1,muServe2,alpha,delta,_,nCores,R.nextInt(),
          n0Prime,accum_sim_t,accum_sample_size)).cache();
      val interOutPut = coreOutPut.reduce((x,y)=>if(x._2>y._2) x else y);
      val maxRound = interOutPut._2;
      
      var tempBestAlts =coreOutPut.collect();
      
      var ss = new Array[Int](nCores);
      
      var set = coreOutPut.collect();
      for(_x<- 0 to nCores-1){
        ss(_x)=set(_x)._1;  
      }
      
      
      
      val coreOutPutFinal = coreID.map(finalSelection(muCall,muServe1,muServe2,alpha,delta,ss,_,maxRound,n0Prime,nCores,R.nextInt(),accum_sim_t,accum_sample_size)).cache();
      
      val finalOutPut = coreOutPutFinal.reduce((x,y)=>if(x._2>y._2) x else y);
      
      val final_t = (System.nanoTime() - start_t).toDouble/1e9;
      //println(f"Total time = $final_t%.2f secs.");
      //println(finalOutPut._1);
      //println(accum_sim_t.value);
      
      outputstring += f"Total time = $final_t%.2f secs."+ " "+finalOutPut._1+
      " "+altVec(finalOutPut._1)(0)+" "+altVec(finalOutPut._1)(1)+" "+altVec(finalOutPut._1)(2)+
      " "+altVec(finalOutPut._1)(3)+" "+altVec(finalOutPut._1)(4)+" "+altVec(finalOutPut._1)(5)+
      " "+altVec(finalOutPut._1)(6)+" "+accum_sim_t.value+" "+accum_sample_size+"\n";
      count+=1;
    }
    val pw = new PrintWriter(new File("Output.txt"));
    pw.write(outputstring);
    pw.close();
    System.out.println(outputstring);
    sc.stop
  }
}