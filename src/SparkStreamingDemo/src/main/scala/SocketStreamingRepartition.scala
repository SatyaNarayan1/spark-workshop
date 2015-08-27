import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
// Create a local StreamingContext with two working thread and batch interval of 1 second.
// The master requires 2 cores to prevent from a starvation scenario.
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
object SocketStreamingRepartition {
  def main(args: Array[String]){
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SocketStreamingRepartition")
      .set("spark.streaming.blockInterval","100")

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)


    def fun (rdd:RDD[String]):Unit ={
      print("partition count"+rdd.partitions.length)
    }

    val ssc = new StreamingContext(conf, Seconds(2))

    val lines = ssc.socketTextStream("localhost", 9998)

    val re_lines=lines.repartition(5)     //extra line for  repartition

    re_lines.foreachRDD(x =>fun(x))

    val words = lines.flatMap(_.split(" "))

    val pairs = words.map(word => (word, 1))

    val wordCounts = pairs.reduceByKey(_ + _)

    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination()



  }
}
