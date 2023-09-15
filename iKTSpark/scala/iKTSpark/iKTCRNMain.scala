package iKTSpark
import scala.util.Random;
import org.apache.spark._;
import org.apache.spark.SparkContext._;
import SparkProgramming.iKTSpark._;
import java.io._;
import java.util.ArrayList



object iKTCRNMain {
  def INI_Sampling(muCall:Double,muServe1:Double,muServe2:Double,
      CoreID:Int,leftAlt:Int,seed:Int,n0Frak:Int,acc_sim_t: Accumulator[Double]):(Array[Int],Array[Double])={

    val y = new iKT_INI_STAGE_CRN(muCall,muServe1,muServe2,CoreID,leftAlt,seed,n0Frak);
    
    y.run();
    
    acc_sim_t += y.getSimulationTime();
    
    
    (y.getSetOfAlt,y.getFrakX);
    
  }
  
  
  
  def withinCoreSelection(alpha:Double,delta:Double,seed:Int,n0:Int,round:Int,coreID:Int,
      nCores:Int,muCall:Double,muServe1:Double,muServe2:Double,I:Array[Int],acc_sim_t: Accumulator[Double],
      acc_sample_size: Accumulator[Double]):(Array[Int],Array[Int],Array[Double])={
    
    val y = new iKTTruckCRN(alpha,delta,seed,n0,round,coreID,nCores,muCall,muServe1,muServe2,I);
    y.run();
    
    
    acc_sim_t += y.getSimulationTime();
    acc_sample_size +=y.getSampleSize;
    
    (y.getBestIDs,y.getT,y.getFrakX);
  }
  
  def finalSelection(muCall:Double,muServe1:Double,muServe2:Double,alpha:Double,delta:Double
      ,altID:Array[Int],coreID:Int,maxRound:Int,n0:Int,nCores:Int,seed:Int,acc_sim_t: Accumulator[Double],
      acc_sample_size: Accumulator[Double]):(Int,Double)={
      
    val rinottSelection = new RinottSelection(muCall, muServe1,muServe2,alpha,delta,nCores,maxRound,
          altID(coreID), seed+coreID,n0);
      rinottSelection.run();
      //System.out.println(altID.length+" "+coreID+" "+altID(coreID)+" "+rinottSelection.getSampleAVG());
      acc_sim_t += rinottSelection.getSimulationTime();
      acc_sample_size +=rinottSelection.getSampleSize;
      (altID(coreID),rinottSelection.getSampleAVG());
  }
  
  def mergeSort(array:Array[Double],I:Array[Int],left:Int,right:Int):Unit={
    if (right <= left) return;
	    val mid = ((left+right)/2).toInt;
	    mergeSort(array, I ,left, mid);
	    mergeSort(array, I ,mid+1, right);
	    merge(array, I ,left, mid, right);
  }
  
  def merge(array:Array[Double],I:Array[Int],left:Int,mid:Int,right:Int):Unit={
    val lengthLeft = mid - left + 1;
    val lengthRight =  right-mid;
    val leftArray  = new Array[Double](lengthLeft);
    val rightArray  = new Array[Double](lengthRight);
    val leftI = new Array[Int](lengthLeft);
    val rightI = new Array[Int](lengthRight);
    
    for (i <- 0 to lengthLeft-1) {
	    	leftArray(i) = array(left+i);
	    	leftI(i)=I(left+i);
	  }
    for (i <- 0 to lengthRight-1) {
	    	rightArray(i) = array(mid+i+1);
	    	rightI(i)=I(mid+i+1);
	  }
    
    var leftIndex = 0;
    var rightIndex = 0;
    for (i <- left to right){
      if (leftIndex < lengthLeft && rightIndex < lengthRight) {
	            if (leftArray(leftIndex) > rightArray(rightIndex)) {
	                array(i) = leftArray(leftIndex);
	                I(i)=leftI(leftIndex);
	                leftIndex=leftIndex+1;
	            }
	            else {
	                array(i) = rightArray(rightIndex);
	                I(i)=rightI(rightIndex);
	                rightIndex=rightIndex + 1;
	            }
	     }
      // if all the elements have been copied from rightArray, copy the rest of leftArray
	    else if (leftIndex < lengthLeft) {
	            array(i) = leftArray(leftIndex);
	            I(i) = leftI(leftIndex);
	            leftIndex=leftIndex +1;
	    }
	        // if all the elements have been copied from leftArray, copy the rest of rightArray
	    else if (rightIndex < lengthRight) {
	            array(i) = rightArray(rightIndex);
	            I(i)=rightI(rightIndex);
	            rightIndex=rightIndex + 1;
	    }
    }
  }
  
  
  def main(args:Array[String]):Unit={
    val muCall = 0.1;
    val muServe1 = 0.05;
    val muServe2 =1d/30;
    val alpha = 0.05;
    val delta = 0.5d/60;
    val n0 = 20;
    val n0Frak = 20;
    val nCores = args(0).toInt;
    val repeat = args(1).toInt;
    val seed = args(2).toInt;
    val conf = new SparkConf().setAppName("iKT-Spark").set("spark.cores.max",nCores.toString());
    val sc = new SparkContext(conf);
    val R = new Random(seed);
    var count =0;  
    var outputstring="";
  
     var numOfAlt = 0;
     var altVec =  Array.ofDim[Int](12376,7);
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
		   
      
    var _s=0L;
		
		  var I = new Array[Int](numOfAlt);
		  for(i <- 0 to numOfAlt-1){
		    I(i)=i;
		  }
		  
		  val start_t = System.nanoTime();
      
		  
		  val coreID = sc.parallelize(0 to nCores-1,nCores);

      val T = new Array[Int](numOfAlt);
      val frakX = new Array[Double](numOfAlt);
      val accum_sim_t = sc.accumulator(0D,"Accumulator: total simulation time");
      val accum_sample_size = sc.accumulator(0D,"Accumulator: total sample size");
      

    
      val initialSeed= Math.ceil(R.nextDouble()*10000);
      val seedFinal = Math.ceil(R.nextDouble()*10000);
      
   
      
      val coreOutPut = coreID.map(INI_Sampling(muCall,muServe1,muServe2,_,nCores,R.nextInt(),
          n0Frak,accum_sim_t)).cache();
      accum_sample_size+=n0Frak*numOfAlt;
      var set = coreOutPut.collect();
      
     
      
      for(_x<- 0 to nCores-1){
        val tempI = set(_x)._1;
        val tempFrakX = set(_x)._2;
        for(i<-0 to tempI.size-1){
         frakX(tempI(i))=tempFrakX(i);
        
         T(tempI(i))=20;
        }
      }
      var round = 1;
      
      while(I.length >= 2 * nCores || (I.length>1 && round<=2)){
      //while(I.length >= 2){  
        val Array = new Array[Double](I.length);
        
        for(i <- 0 to I.length-1) {
				  Array(i)=frakX(I(i));
			  }
        
       /** var idx = 0;
        var max = frakX(I(0));
        for(i <- 1 to I.length-1){
          if(frakX(I(i))>max){
            idx = i;
            max = frakX(I(i));
          }
        }
        val tempMax = I(idx);
        I(idx)=I(0);
        I(0)=tempMax;**/
        
        
        
			  mergeSort(Array,I,0,I.length-1);
	
			  val withinCoreOutPut = coreID.map(withinCoreSelection(alpha,delta,R.nextInt(),n0,round,_,
        nCores,muCall,muServe1,muServe2,I,accum_sim_t,accum_sample_size)).cache();
			 
			  var tempSet = withinCoreOutPut.collect();
			   
			  var interMediateSize = 0;
			  for(i <-0 to nCores - 1){
			    val tempB = tempSet(i)._1;
			    interMediateSize = interMediateSize+tempB.length;
			  }
			  val tempM = new Array[Int](interMediateSize);
			  var _c=0;
			  for(i <-0 to nCores - 1){
			    val tempB = tempSet(i)._1;
			    val tempBT = tempSet(i)._2;
			    val tempBX = tempSet(i)._3;
			    for(j <- 0 to tempB.length-1){
			      tempM(_c)=tempB(j);
			    
			      frakX(tempB(j))=(frakX(tempB(j))*T(tempB(j))+tempBX(j)*tempBT(j))/(T(tempB(j))+tempBT(j));
			    
			      T(tempB(j))=T(tempB(j))+tempBT(j);
			      _c=_c+1;
			    }
			    
			  }
			  I = tempM;
			  round = round+1;
			  
      }
      val st =  System.nanoTime();
      var finalOutPut = -1;
      if(I.length>1){
        ///var temp = I.length;
        //if(I.length>nCores){
          //temp=nCores
        //}
        //val coreID2 = sc.parallelize(0 to temp-1, temp);
       
        
        val coreID2 = sc.parallelize(0 to I.length-1, nCores);
        //val coreID2 = sc.parallelize(0 to I.length-1, I.length);
        val coreOutPutFinal = coreID2.map(finalSelection(muCall,muServe1,muServe2,alpha,delta,I,_,round,n0,I.length,R.nextInt(),accum_sim_t,accum_sample_size)).cache();
      
        val tempFinal = coreOutPutFinal.reduce((x,y)=>if(x._2>y._2) x else y);
        finalOutPut =tempFinal._1;
      }else{
        finalOutPut = I(0);
      }
      _s=_s+System.nanoTime()-st;
      val final_t = (System.nanoTime() - start_t).toLong/1e9;
      
     // val final_t = (System.nanoTime() - start_t).toDouble/1e9;
      System.out.println(final_t);
      outputstring += f"Total time = $final_t%.2f secs."+ " "+finalOutPut+
      " "+altVec(finalOutPut)(0)+" "+altVec(finalOutPut)(1)+" "+altVec(finalOutPut)(2)+
      " "+altVec(finalOutPut)(3)+" "+altVec(finalOutPut)(4)+" "+altVec(finalOutPut)(5)+
      " "+altVec(finalOutPut)(6)+" "+accum_sample_size+" "+accum_sim_t.value/1e9+"\n";
      count+=1;
      System.out.println(_s);
      
		}
		val pw = new PrintWriter(new File("Output.txt"));
    pw.write(outputstring);
    pw.close();
    
    System.out.println(outputstring);
    sc.stop
 
  }
}