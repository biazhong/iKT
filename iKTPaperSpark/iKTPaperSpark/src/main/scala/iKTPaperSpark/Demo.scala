package iKTPaperSpark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
object Demo {
  def main(args:Array[String]){
    val conf = new SparkConf()
    conf.set("Spark.master","local")
    conf.set("Spark.app.name","DemoSparkApp")
    val sc = new SparkContext(conf)
    val r1 = sc.range(1,100)
    r1.collect.foreach(println)
    sc.stop
  }
}