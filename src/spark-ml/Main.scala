
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by sachinparmar on 21/08/15.
 */
object Main extends App {

  // spark context
  val sparkConf = new SparkConf().setAppName("Main").setMaster("local[2]")
  val sc = new SparkContext(sparkConf)

  // sql context
  val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._

  val dataDir = "/Users/sachinparmar/my/work/myCode/spark-ml-demo/sample-data/"

  // training data set
  case class Review(rating: Double, review: String, label: Double)
  val training_data = dataDir+"review.csv"

  // load training data and create data frame
  val reviews = sc.textFile(training_data).map(_.split(",")).map(r => Review(r(0).trim.toDouble, r(1), r(2).trim.toDouble)).toDF()
  println("[*] Training data set .....")
  reviews.take(10)

  /*
+------+--------------------+-----+
|rating|              review|label|
+------+--------------------+-----+
|   4.5|this is a amazing...|  1.0|
|   1.0|i want to return ...|  0.0|
|   4.0|     amazing product|  1.0|
|   0.5|      return product|  0.0|
+------+--------------------+-----+
   */

  // stage#1 - transformer
  val tokenizer = new org.apache.spark.ml.feature.Tokenizer().setInputCol("review").setOutputCol("words")

  // stage#2 - transformer
  val hashingTF = new org.apache.spark.ml.feature.HashingTF().setInputCol(tokenizer.getOutputCol).setOutputCol("features")

  // stage#3 - estimator
  val lr = new org.apache.spark.ml.classification.LogisticRegression().setMaxIter(10).setRegParam(0.01)

  // create a pipeline with these 3 stages
  val pipeline = new org.apache.spark.ml.Pipeline().setStages(Array(tokenizer, hashingTF, lr))

  // fit the model
  val model = pipeline.fit(reviews)

  // apply the model on training data set
  println("[*] Data set after model transformation (on training data set) .....")
  model.transform(reviews).select("label", "review", "prediction", "probability", "rawPrediction").show()

  // test data set
  case class Test(rating: Double, review: String)

  // load test data and create data frame
  val test_data = dataDir+"testData.csv"
  val testData = sc.textFile(test_data).map(_.split(",")).map(r => Test(r(0).trim.toDouble, r(1))).toDF()
  println("[*] Test data set .....")
  testData.take(10)

  // apply the model on test data set
  println("[*] Data set after model transformation (on test data set) .....")
  model.transform(testData).select("rating", "prediction", "probability", "review", "rawPrediction").show()
}
