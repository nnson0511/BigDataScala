package example

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.NGram
import _root_.org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

class Tweeter {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder().appName("SentimentAnalysis").master("local[*]").getOrCreate()
        spark.sparkContext.setLogLevel("ERROR")

        val df = spark.read.parquet("/home/ubuntu/Personal/Spark_Big_Data/Lab03/sa.parquet")
        val tokenizer = new Tokenizer().setInputCol("tweet").setOutputCol("tokens")
        val ngrams = new NGram().setInputCol("tokens").setOutputCol("bigrams").setN(2)

        val vectorizer = new Countvectorizer().setInputCol("bigrams").setOutputCol("x").setMinDF(5)
        val idf = new IDF().setInputCol("x").setOutputCol("features")
        val logreg = new LogisticRegression().setFeaturesCol("features").setLabelCol("sentiment")
        val pipeline = new Pipeline().setStages(Array(tokenizer, vectorizer, idf, logreg))

        val model = pipeline.fit(df)
        val ef = model.transform(df)

        ef.select("sentiment", "prediction").show()

        val evaluator = new MulticlassClassificationEvaluator().setLabelCol("sentiment")
        val f1 = evaluator.evaluate(ef)
        printf(s"F1 score = $f1")

        spark.stop() 
    }
}
