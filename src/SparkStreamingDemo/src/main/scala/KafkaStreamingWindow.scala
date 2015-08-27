/**
 * Created by sachin on 8/22/15.
 */

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

object KafkaStreamingWindow {
  def main(args: Array[String]){

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)


    val zkQuorum="localhost:2181";
    val group="test";
    val topics="test";
    val numThreads="1";

    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("KafkaStreaming")

    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)

    val words = lines.flatMap(_.split(" "))

    val pairs = words.map(word => (word, 1))

    //reduceFunc,invReduceFunc,windowDuration,slideDuration,numPartitions

    val wordCounts = pairs.reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)

    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination()
  }
}