/**
 * Created by sachin on 8/22/15.
 */

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

object KafkaStreaming {
  def main(args: Array[String]){

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)


    val zkQuorum="localhost:2181";
    val group="test";
    val topics="test";
    val numThreads="1";    //partitions, Each partition is consumed in its own thread. Topic partitions in Kafka does not correlate to partitions of RDDs generated in Spark Streaming

    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("KafkaStreaming")

    val ssc = new StreamingContext(sparkConf, Seconds(2))       //batch duration 2s

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

    val l = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)//.map(_._2)
    println(l)

    val lines=l.map(_._2)
    println(lines)

    val words = lines.flatMap(_.split(" "))

    val pairs = words.map(word => (word, 1))

    val wordCounts = pairs.reduceByKey(_ + _)

    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination()
  }
}