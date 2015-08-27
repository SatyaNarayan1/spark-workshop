/**
 * Created by sachin on 8/26/15.
 */
  package org.apache.spark.examples.streaming

  import kafka.serializer.StringDecoder
  import org.apache.log4j.{Level, Logger}

  import org.apache.spark.streaming._
  import org.apache.spark.streaming.kafka._
  import org.apache.spark.SparkConf

  /**
   * Consumes messages from one or more topics in Kafka and does wordcount.
   * Usage: DirectKafkaWordCount <brokers> <topics>
   *   <brokers> is a list of one or more Kafka brokers
   *   <topics> is a list of one or more kafka topics to consume from
   *
   * Example:
   *    $ bin/run-example streaming.DirectKafkaWordCount broker1-host:port,broker2-host:port \
   *    topic1,topic2
   */
  object DirectKafkaStreaming {
    def main(args: Array[String]) {


      Logger.getLogger("org").setLevel(Level.OFF)
      Logger.getLogger("akka").setLevel(Level.OFF)

      val brokers="localhost:9092";
      val topics="test";


      // Create context with 2 second batch interval
      val sparkConf = new SparkConf()
        .setMaster("local[2]")
        .setAppName("DirectKafkaWordCount")

      val ssc = new StreamingContext(sparkConf, Seconds(2))

      // Create direct kafka stream with brokers and topics
      val topicsSet = topics.split(",").toSet

      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, topicsSet)

      // Get the lines, split them into words, count the words and print
      val lines = messages.map(_._2)

      val words = lines.flatMap(_.split(" "))
      val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
      wordCounts.print()
      // Start the computation
      ssc.start()
      ssc.awaitTermination()
    }
  }
