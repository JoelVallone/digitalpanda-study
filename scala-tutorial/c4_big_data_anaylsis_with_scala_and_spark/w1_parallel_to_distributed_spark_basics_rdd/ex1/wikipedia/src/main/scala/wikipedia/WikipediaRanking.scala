package wikipedia

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

import org.apache.spark.rdd.RDD

case class WikipediaArticle(title: String, text: String) {
  /**
    * @return Whether the text of this article mentions `lang` or not
    * @param lang Language to look for (e.g. "Scala")
    */
  def mentionsLanguage(lang: String): Boolean = text.split(' ').contains(lang)
}

object WikipediaRanking {

  val langs = List(
    "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
    "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

  val conf: SparkConf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("Wikipedia Ranking - JVA")
    .set("spark.driver.bindAddress", "127.0.0.1")
  val sc: SparkContext = new SparkContext(conf)
  // Hint: use a combination of `sc.textFile`, `WikipediaData.filePath` and `WikipediaData.parse`
  val wikiRdd: RDD[WikipediaArticle] = sc
      .textFile(WikipediaData.filePath,4)
      .map(WikipediaData.parse)

  /** Returns the number of articles on which the language `lang` occurs.
   *  Hint1: consider using method `aggregate` on RDD[T].
   *  Hint2: consider using method `mentionsLanguage` on `WikipediaArticle`
   */
  def occurrencesOfLang(lang: String, rdd: RDD[WikipediaArticle]): Int =
    rdd.aggregate(0)((count: Int, article) => if (article.mentionsLanguage(lang)) count + 1 else count , _+_)

  /* (1) Use `occurrencesOfLang` to compute the ranking of the languages
   *     (`val langs`) by determining the number of Wikipedia articles that
   *     mention each language at least once. Don't forget to sort the
   *     languages by their occurrence, in decreasing order!
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangs(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] =
    langs
      .map(lang => (lang, occurrencesOfLang(lang, rdd))) // On worker nodes
      .sortBy(-1 * _._2) // On driver node

  /* Compute an inverted index of the set of articles, mapping each language
   * to the Wikipedia pages in which it occurs.
   */
  def makeIndex(langs: List[String], rdd: RDD[WikipediaArticle]): RDD[(String, Iterable[WikipediaArticle])] =
    rdd.flatMap( article =>
        langs
          .filter( lang => article.mentionsLanguage(lang))
          .map(lang => (lang,article)))
      .groupByKey()
      .persist()
  /* Out of memory error as cartesian product enumerates all combinations
    sc.parallelize(langs) // All on worker nodes
      .cartesian(rdd)
      .filter{ case (lang, article) => article.mentionsLanguage(lang)}
      .groupByKey()
      .persist()
  */

  /* (2) Compute the language ranking again, but now using the inverted index. Can you notice
   *     a performance improvement?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangsUsingIndex(index: RDD[(String, Iterable[WikipediaArticle])]): List[(String, Int)] =
    index
      .mapValues(_.size) // On worker nodes
    .collect()
      .sortBy(-1 * _._2).toList // On driver node

  /* (3) Use `reduceByKey` so that the computation of the index and the ranking are combined.
   *     Can you notice an improvement in performance compared to measuring *both* the computation of the index
   *     and the computation of the ranking? If so, can you think of a reason?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangsReduceByKey(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] =
    (for {
      article <- rdd
      lang <- langs if article.mentionsLanguage(lang)
    } yield (lang, 1)).reduceByKey(_+_) // On worker nodes
  .collect()  // On driver node
    .sortBy(-1 * _._2)
    .toList

def main(args: Array[String]) {
  //Expected Languages ranked:  List((JavaScript,1692), (C#,705), (Java,586), (CSS,372), (C++,334), (MATLAB,295), (Python,286), (PHP,279), (Perl,144), (Ruby,120), (Haskell,54), (Objective-C,47), (Scala,43), (Clojure,26), (Groovy,23))
  /* Languages ranked according to (1) */
  val langsRanked: List[(String, Int)] = timed("Part 1: naive ranking", rankLangs(langs, wikiRdd))
    println(f"Languages ranked by existence in article: $langsRanked")
  /* An inverted index mapping languages to wikipedia pages on which they appear */
  def index: RDD[(String, Iterable[WikipediaArticle])] = makeIndex(langs, wikiRdd)

  /* Languages ranked according to (2), using the inverted index */
  val langsRanked2: List[(String, Int)] = timed("Part 2: ranking using inverted index", rankLangsUsingIndex(index))
  println(f"Languages ranked by existence in inverted index: $langsRanked2")

  /* Languages ranked according to (3) */
  val langsRanked3: List[(String, Int)] = timed("Part 3: ranking using reduceByKey", rankLangsReduceByKey(langs, wikiRdd))
  println(f"Languages ranked by existence in fast inverted index: $langsRanked3")

  /* Output the speed of each ranking */
  println(timing)
  sc.stop()
}

val timing = new StringBuffer
def timed[T](label: String, code: => T): T = {
  val start = System.currentTimeMillis()
  val result = code
  val stop = System.currentTimeMillis()
  timing.append(s"Processing $label took ${stop - start} ms.\n")
  result
}
}
