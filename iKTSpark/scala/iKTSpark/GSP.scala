package iKTSpark
import scala.util.Random;

import org.apache.spark._
import org.apache.spark.SparkContext._
import SparkProgramming.iKTSpark._
import java.io._;




object GSP {
	@SerialVersionUID(123L)
	class sysEntry(id_in: Int, fn_in: Double, n_in: Int,
			bsize_in: Int, S2_in: Double, stream_in: String, rmax_i_in: Int = -1) extends Serializable	
			{
		var id: Int = id_in;
	var fn: Double = fn_in;
	var n: Int = n_in;
	val bsize: Int = bsize_in;
	val S2: Double = S2_in;
	var stream: String = stream_in;
	var rmax_i: Int = rmax_i_in;

	override def toString() : String = {
			val collected = (",",id, fn, n, bsize, S2, stream, rmax_i,",");
			collected.toString();
	}

	def eliminates(j: sysEntry, n1: Int, rmax: Int, r_current: Int = -1, eta: Double) : Boolean = {
			val _rmax = if(rmax > 0) rmax else if (this.rmax_i > j.rmax_i) j.rmax_i else this.rmax_i;
			val skip = (r_current > _rmax && rmax <= 0);
			if (skip) {
				false
			} else {
				val Xbar_j = j.fn;
				val nj = j.n;
				val S2j = j.S2;
				val bj = j.bsize;
				val Xbar_i = this.fn;
				val ni = this.n;
				val S2i = this.S2;
				val bi = this.bsize;
				val maxi = (if (rmax > 0) n1 else 0) + _rmax * bi;
				val maxj = (if (rmax > 0) n1 else 0) + _rmax * bj;
				val tij = 1.0/ ( (S2i / maxi) + ( S2j / maxj ) );
				val aij = Math.sqrt( tij * (n1 - 1.0) ) * eta;
				val tau = 1.0/( (S2i/ni)+(S2j/nj) );
				val Y = tau*(Xbar_i-Xbar_j);
				(Y > aij)
			}
	}
			}

	def stage1mapper(id: Int, n1: Int, globalSeed: Long,
			muCall: Double, muServe1: Double, muServe2: Double, altVec:Array[Array[Int]] , 
			acc_sim: Accumulator[Long], acc_t: Accumulator[Long])
	: (Int, Double, Double, Long,String) = {
		
	  val tempR = new Random();
	  tempR.setSeed(id+globalSeed);
	  
	  val tempResults = new Array[Double](n1);
	  val tempAltVec = new Array[Double](7);
	  for(i<-0 to 6){
	    tempAltVec(i)=altVec(id)(i);
	  }
	  var run_time =0L;
	  var fn = 0d;
	  var s2 = 0d;
	  val start_time = System.nanoTime()
	  for(i <- 0 to n1-1){
		val prob = new genTruckObv(muCall,muServe1,muServe2,tempR.nextLong().toInt+i);
		prob.setAlt(tempAltVec);
		
		prob.run()
		
		tempResults(i) = prob.getAVGWaitingTime(); 
		fn = fn+tempResults(i);
	  }
	  run_time = System.nanoTime() - start_time
	  acc_t += run_time
		acc_sim += n1.toLong
	  
	  
		fn = fn/n1;
		for(i <-0 to n1-1){
		  s2 =  s2+(tempResults(i)-fn)*(tempResults(i)-fn);
		}
		
		s2 = s2/(n1-1)
		(id, fn, s2, run_time,"13345")
	}

	def batchsizemapper( entry: (Int, Double, Double, Long,String),
			n1: Int, nGroups: Int, avgS: Double, batchSize: Int, 
			maxS: Double, maxMU: Double, delta: Double, eta: Double, h_cap: Double)
	: ( Int, sysEntry ) = {
		val groupID = entry._1%nGroups;
		val S2 = entry._3;
		val bsize = Math.ceil(Math.sqrt(S2) / avgS * batchSize).toInt;
		val e = new sysEntry(entry._1, entry._2, n1, bsize, entry._3,entry._5);
		assert(e.rmax_i < 0);
		if(maxS > 0) {
      val stdev = Math.sqrt((S2 + maxS)/2) * Math.sqrt(2.0/(n1-1.0));
			e.fn = 0.0;
			e.n = 0;
			val diff = Math.max(maxMU - entry._2 - 2 * stdev, delta);
			e.rmax_i = Math.ceil((n1-1.0) * ( Math.sqrt(S2) + maxS ) * 
					eta * eta / batchSize * avgS / diff / diff).toInt;
			val rinott_r_cap = Math.ceil(1.0 * h_cap * h_cap * S2 / delta / delta / bsize).toInt;
			e.rmax_i = Math.min(e.rmax_i, rinott_r_cap);
			assert (e.rmax_i > 0);
		}
		( groupID, e )
	}

	def runSimBatch( e: sysEntry, nGroups: Int, globalSeed: Long,muCall:Double, muServe1:Double,muServe2:Double,
	    altVec:Array[Array[Int]],acc_sim: Accumulator[Long], acc_t: Accumulator[Long], r_current: Int )
	: (Int, sysEntry ) = {
		val new_e = if (e.rmax_i > 0 && r_current > e.rmax_i) {
			e
		} else {
		  val tempR = new Random();
		  tempR.setSeed(e.id+globalSeed);
		  val tempResults = new Array[Double](e.bsize);
	    val tempAltVec = new Array[Double](7);
	    for(i<-0 to 6){
	      tempAltVec(i)=altVec(e.id)(i);
	    }
	    
	    var run_time =0L;
	    var fn = 0d;
	    var s2 = 0d;
	    val start_time = System.nanoTime()
	    for(i <- 0 to e.bsize-1){
		  val prob = new genTruckObv(muCall,muServe1,muServe2,tempR.nextLong().toInt+i);
		  prob.setAlt(tempAltVec);
		    
		    prob.run()
		    
		    tempResults(i) = prob.getAVGWaitingTime(); 
		    fn = fn+tempResults(i);
	    }
	    run_time = System.nanoTime() - start_time
	    acc_t += run_time
		  acc_sim += e.bsize.toLong
	    fn = fn/(e.bsize);
		  for(i <-0 to e.bsize-1){
		    s2 =  s2+(tempResults(i)-fn)*(tempResults(i)-fn);
		  }
		  s2 = s2/(e.bsize-1)
		  val new_n = e.n + e.bsize;
			val new_fn = (fn * e.bsize + e.fn * e.n) / new_n;
		  
			new sysEntry( e.id, new_fn, new_n, e.bsize, e.S2, "13345", e.rmax_i );
		}
		val groupID = e.id%nGroups ;
		( groupID, new_e )
	}

	def calcRinottSample( e: sysEntry, delta: Double,
			batchSize: Int, rinott_h: Double ) 
	: List[ (Int, (String, Int)) ] = {
		var out: List [ (Int, (String, Int)) ] = List();
	val RinottSize = Math.ceil(rinott_h * rinott_h * e.S2 / delta / delta).toInt;
	var additionalSize = RinottSize - e.n;
	additionalSize = if (additionalSize > 0) additionalSize else 0;
	var seed = RngStream.StrToSeed(e.stream);
	var rStream = new RngStream();
	rStream.setSeed(seed);
	while(additionalSize > 0) {
		val state = rStream.getState();
		seed = state.map( _.toLong );
		val seedStr = RngStream.SeedToStr(seed);
		val nextBatchSize = if (additionalSize > batchSize) batchSize else additionalSize;
		additionalSize -= nextBatchSize;
		out = out :+ ( e.id, (seedStr, nextBatchSize) );
		rStream.resetNextSubstream();
	}
	out
	}

	def runRinottSim( e: (Int, (String, Int)), globalSeed:Long,muCall:Double,muServe1:Double,muServe2:Double,
	    altVec: Array[Array[Int]], acc_sim: Accumulator[Long], acc_t: Accumulator[Long], run: Boolean )
	: (Int, (Double, Int) ) = {
		val sysid = e._1;
		val size = e._2._2;
		assert(size > 0);
		
		val tempR = new Random();
	  tempR.setSeed(sysid+globalSeed);
	  
	  val tempResults = new Array[Double](size);
	  val tempAltVec = new Array[Double](7);
	  for(i<-0 to 6){
	    tempAltVec(i)=altVec(sysid)(i);
	  }
	  var run_time =0L;
	  var fn = 0d;
	  var s2 = 0d;
		val start_time = System.nanoTime()
		for(i <- 0 to size-1){
		  val prob = new genTruckObv(muCall,muServe1,muServe2,tempR.nextLong().toInt+i);
		  prob.setAlt(tempAltVec);
		  
		  prob.run()
		  
		  tempResults(i) = prob.getAVGWaitingTime(); 
		  fn = fn+tempResults(i);
	  }
		run_time = run_time+System.nanoTime() - start_time
		fn = fn/size;
		for(i <-0 to size-1){
		  s2 =  s2+(tempResults(i)-fn)*(tempResults(i)-fn);
		}
		
	  acc_t += run_time
		acc_sim += size.toLong
		(sysid, (fn, size));
	}

	def existingSample( e: sysEntry ) : (Int, (Double, Int) ) = {
		( e.id, (e.fn, e.n) )
	}

	def toList[A](a: A) = List(a);

	def ListVsEntry( sysList: List[sysEntry], nextSys: sysEntry,
			n1: Int, rmax: Int, r_current: Int, eta: Double )
	: List[sysEntry] = {
			val elim = sysList.exists( i => i.eliminates(nextSys, n1, rmax, r_current, eta));
			var newList = sysList.filter( i => !(nextSys.eliminates(i, n1, rmax, r_current, eta)));
			if (!elim) newList = newList :+ nextSys;
			newList
	}

	def ListVsList( aList: List[sysEntry], bList: List[sysEntry],
			n1: Int, rmax: Int, r_current: Int, eta: Double )
	: List[sysEntry] = {
			val aList_new = aList.filter{
				i => !(bList.exists( j => j.eliminates(i, n1, rmax, r_current, eta)))
			};
			val bList_new = bList.filter{
				j => !(aList.exists( i => i.eliminates(j, n1, rmax, r_current, eta)))
			};
			aList_new ::: bList_new
	}

	//	def ListVsBest( t: (Int, (List[sysEntry], Option[Iterable[sysEntry]])),
	//			n1: Int, rmax: Int, eta: Double)
	//			: List[sysEntry] = {
	//		if (t._2._1.isEmpty) Nil else {
	//			val aList = t._2._1;
	//			val bList = t._2._2;
	//			bList match {
	//			case Some( l ) =>
	//			aList.filter( i => !(l.exists( j => j.eliminates(i,n1,rmax,eta))))
	//			case None => aList
	//			}
	//		}
	//	}

	def main(args: Array[String]) = {
		
		val outputFile = "GSPOutput.txt";
		val n1 = 20;
		val batchSize = 200;
		
		val nCores= args(0).toInt;
		val nGroups = nCores;
		val repeat = args(1).toInt;
		val seed = args(2).toInt;
		val s1Max = 1000;
		val delta = 1d/60;
		val muCall = 0.1;
    val muServe1 = 0.05;
    val muServe2 =1d/30;
		val runS2 = true;
		val runS3 = true && runS2;
		
		val numSys = 12376;
		val eta =  EtaFunc.find_eta(n1, 0.025, numSys);
		
		val rinott_h_cap = Rinott.rinott(Math.ceil(numSys * 0.03).toInt, 0.975, n1-1);
		val rmax = if (s1Max > 0) (s1Max / batchSize) else -1;
		val r_tol = 500;
		println( f"Eta=$eta, rmax=$rmax, nGroups=$nGroups, numSys=$numSys, h=$rinott_h_cap");
		val conf = new SparkConf().setAppName("RnS-Spark").set("spark.cores.max", nCores.toString());;
		var r_current = 0;
		val f1 = toList[sysEntry](_);
		val f2 = (sysList: List[sysEntry], nextSys: sysEntry ) => ListVsEntry( sysList, nextSys, n1, rmax, r_current, eta );
		val f3 = (a: List[sysEntry], b: List[sysEntry]) => ListVsList( a, b, n1, rmax, r_current, eta );
		
		val R = new Random(seed);
		
		
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
		var outputstring="";
		val sc = new SparkContext(conf);
		
		for(count<- 0 to repeat -1){
		  val start_t = System.nanoTime();
		  // val groupsList = (0 to (nGroups - 1)).toList;
		// Initialize a list of systems
		val sysIDs = sc.parallelize((0 to (numSys - 1)).toList, nGroups * 1 );
		// Declare various accumulators for profiling
		val accum_S1_sim = sc.accumulator(0L, "Accumulator: Stage 1 Simulations");
		val accum_S2_sim = sc.accumulator(0L, "Accumulator: Stage 2 Simulations");
		val accum_S3_sim = sc.accumulator(0L, "Accumulator: Stage 3 Simulations");
		val accum_S1_t = sc.accumulator(0L, "Accumulator: Stage 1 Run Time");
		val accum_S2_t = sc.accumulator(0L, "Accumulator: Stage 2 Run Time");
		val accum_S3_t = sc.accumulator(0L, "Accumulator: Stage 3 Run Time");
		val accum_Screen_t = sc.accumulator(0L, "Accumulator: Screen Time");
		val globalSeed = R.nextLong();
		val R1 = new Random();
		R1.setSeed(globalSeed);
		// Create a Scala Spark Context.
		
	
		// Stage 1: Simulate each system for n1 replications
		println("Entering Stage 1...");
		val stage1output = sysIDs.map(
				stage1mapper( _, n1, R1.nextLong(), muCall, muServe1,muServe2,altVec, accum_S1_sim, accum_S1_t )
				).cache();
		
		
		val avgS = stage1output.map( x=>Math.sqrt(x._3) ).reduce(_+_) / numSys;
		var maxS = -1.0;
		var maxMU = -1.0;
		if(s1Max < 0) {
			maxMU = stage1output.map( x=>x._2 ).reduce((x,y) => if (x > y) x else y);
			maxS =  stage1output.map( x=>Math.sqrt(x._3) ).reduce((x,y) => if (x > y) x else y);
			println(f"MaxMU=$maxMU, MaxS=$maxS");
			assert(maxS > 0);
		}

		var simoutput = stage1output.map{
			batchsizemapper( _, n1, nGroups, avgS, batchSize, maxS, maxMU, delta, eta, rinott_h_cap ) 
		};
		var iterationEndOutput = if (s1Max >= 0) {
			val screenoutput = simoutput.combineByKey( f1, f2, f3 );
			//    var bestsys = screenoutput.mapValues( l => l.reduce(
			//        (x: sysEntry, y: sysEntry) => if (x.fn > y.fn) x else y
			//        )).values.flatMap(
			//            e => { groupsList.map( i => (i, e) ) }
			//            ).groupByKey();
			val bestsys = sc.broadcast( 
					screenoutput.values.filter{
						_ != Nil 
					}.map{           
						l => l.reduce{
							(x, y) => if (x.fn > y.fn) x else y
						}
					}.collect() );
			screenoutput.values.flatMap{ x => x }.filter{ 
				x => !( bestsys.value.exists (_.eliminates(x, n1, rmax, r_current, eta)) ) 
			}.cache();
		} else {
			simoutput.map(_._2).cache();
		}
		//		var bestList = screenoutput.leftOuterJoin(bestsys);
		//		var screenBestOutput = bestList.flatMap(
		//				ListVsBest(_, n1, rmax, eta) ).cache();
		var surviving = iterationEndOutput.count;
		r_current = 1;
		var bestSys = -1;
		val sim_S1 = accum_S1_sim.value;
		val t_S1 = accum_S1_t.value / 1e9;
		var sim_S2 = 0.0;
		var t_S2 = 0.0;
		println("Stage 1 total simulations = " + sim_S1);
		println("Stage 1 total time = " + t_S1);

		if (!runS2) 		{  
			iterationEndOutput.saveAsTextFile(outputFile);
			stage1output.saveAsTextFile(outputFile+"1");
		}

		if(surviving == 1) {
			println("!Found Answer!");
			bestSys = iterationEndOutput.first().id; 
			println(bestSys);
		}

		val rmaxmax = if(rmax > 0) rmax else {
			iterationEndOutput.map(_.rmax_i).top(2).last;
		}
		println(f"Max $rmaxmax iterations in Stage 2 ");

		val q = new scala.collection.mutable.Queue[Long];
		var q_break = false;
		if(runS2 && surviving > 1) {
			println("Entering Stage 2...");
			while (r_current <= rmaxmax && surviving > 1 && !q_break) {
				val updatedSimOutput = iterationEndOutput.repartition(nGroups * 1).map(
						runSimBatch(_, nGroups, R1.nextLong,muCall, muServe1,muServe2,altVec, accum_S2_sim, accum_S2_t, r_current) );
				val screenoutput = updatedSimOutput.combineByKey( f1, f2, f3 );
				//        bestsys = screenoutput.filter( _._2 != Nil ).mapValues(
				//            l => l.reduce(
				//                (x: sysEntry, y: sysEntry) => if (x.fn > y.fn) x else y
				//                )
				//            ).values.flatMap(
				//                e => {
				//                  groupsList.map( i => (i, e) )
				//                }
				//                ).groupByKey();
				val bestsys = sc.broadcast( 
						screenoutput.values.filter{
							_ != Nil 
						}.map{           
							l => l.reduce{
								(x, y) => if (x.fn > y.fn) x else y
							}
						}.collect() );

				iterationEndOutput = screenoutput.values.flatMap{ x => x }.filter{ 
					x => !( bestsys.value.exists (_.eliminates(x, n1, rmax, r_current, eta)) ) 
				}.cache();
				//				bestList = screenoutput.leftOuterJoin(bestsys);
				//				screenBestOutput = bestList.flatMap(
				//						ListVsBest(_, n1, rmax, eta) ).cache();
				surviving = iterationEndOutput.count;
				println(f"$surviving systems remaining after round $r_current.");
				r_current += 1;
				if (r_tol > 0 && rmax < 0) {
					if (q.length >= r_tol - 1) {
						val last_surviving = q.dequeue();
						if (last_surviving == surviving)  {
							println(f"No system eliminated in the last $r_tol iterations. Breaking from Stage 2!")
							q_break = true;
						}

					}
					q += surviving;
				}
			}
			sim_S2 = accum_S2_sim.value;
			t_S2 = accum_S2_t.value / 1e9;
			println("Stage 2 total simulations = " + sim_S2);
			println("Stage 2 total time = " + t_S2);
		}

		val p2Sys = surviving;
		println(f"$surviving systems remaining after stage 2.");

		if(surviving == 1) {
			println("!Found Answer!");
			bestSys = iterationEndOutput.first().id;
			println(bestSys);
		}

		if (surviving > 1) {
			println(f"Entering Stage 3...");
			val rinott_h = Rinott.rinott(numSys, 0.975, n1-1);
			println(f"Rinott constant = $rinott_h");
			val extraSims = iterationEndOutput.flatMap{ 
				calcRinottSample(_, delta, batchSize, rinott_h)
			}.repartition(nGroups * 1).map{ 
				runRinottSim(_, R1.nextLong(),muCall, muServe1,muServe2,altVec, accum_S3_sim, accum_S3_t, runS3 ) 
			};
			val allSims = iterationEndOutput.map{
				existingSample(_) 
			}.union(extraSims).reduceByKey(
					(x, y) => {
						val sz = x._2 + y._2;
						( (x._1 * x._2 + y._1 * y._2) / sz, sz );
					}
					);
			bestSys = allSims.reduce{
				(x, y) => if ( x._2._1 > y._2._1 ) x else y
			}._1;
			println("!Found Answer!");
			println(bestSys);
		}
		val final_t = (System.nanoTime() - start_t).toDouble/1e9;
		println(f"Total time = $final_t%.2f secs.");
		val sim_S3 = accum_S3_sim.value;
		val sim_Total = sim_S1 + sim_S2 + sim_S3;
		val t_S3 = accum_S3_t.value / 1e9;
		val t_Total = t_S1 + t_S2 + t_S3;
		val sim_util = t_Total / nCores / final_t;
		val finalOutPut = bestSys;
		outputstring += f"Total time = $final_t%.2f secs."+ " "+finalOutPut+
      " "+altVec(finalOutPut)(0)+" "+altVec(finalOutPut)(1)+" "+altVec(finalOutPut)(2)+
      " "+altVec(finalOutPut)(3)+" "+altVec(finalOutPut)(4)+" "+altVec(finalOutPut)(5)+
      " "+altVec(finalOutPut)(6)+" "+sim_Total+" "+t_Total +"\n";
/**		println("algo,n0,n1,bsize,bmax,delta,seed," + 
				"screenVer,core,time,RB,cov,systems,phase2sys," +
				"simcount_0,simcount_1,simcount_2,simcount_3,totalsim," +
				"winner,simtime_0,simtime_1,simtime_2,simtime_3," + 
				"simtime_total,simtime_util,screentime,screentime_util");
		println(f"SPK_GSP,0,$n1,$batchSize,$rmax,$delta,$globalSeed," + 
				f"8,$nCores,$final_t%.2f,$numSys,$p2Sys," + 
				f"0,$sim_S1,$sim_S2,$sim_S3,$sim_Total," + 
				f"$bestSys,0.0,$t_S1%.2f,$t_S2%.2f,$t_S3%.2f," +
				f"$t_Total%.2f,$sim_util,0,0");**/
	}
		
		val pw = new PrintWriter(new File("Output.txt"));
    pw.write(outputstring);
    pw.close();
    System.out.println(outputstring);
	}
}
