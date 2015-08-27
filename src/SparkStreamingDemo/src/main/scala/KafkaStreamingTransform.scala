import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils

// Create a local StreamingContext with two working thread and batch interval of 1 second.
// The master requires 2 cores to prevent from a starvation scenario.
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf

object KafkaStreamingTransform {
  def main(args: Array[String]){
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("KafkaStreamingTransform")
      .set("spark.streaming.blockInterval","100")

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val zkQuorum="localhost:2181";
    val group="test";
    val topics="test";
    val numThreads="1";

    val ssc = new StreamingContext(conf, Seconds(2))

    val joinFile = ssc.sparkContext.textFile("/Users/sachin/Documents/github/SparkStreamingDemo/data.txt")
    val data = joinFile.map(line => line.split(",")).map(e=>(e(0),e(1)))
    data.collect.foreach(println)

    print(data.collect().foreach(x=>println(x)))

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)

    val words = lines.flatMap(_.split(" "))

    val pairs = words.map(word => (word, 1))

    val wordCounts = pairs.reduceByKey(_ + _)

    wordCounts.print()

    val cleanedDStream = wordCounts.transform(rdd => {rdd.join(data)})

    cleanedDStream.print

    ssc.start()
    ssc.awaitTermination()



  }
}
