name := "observatory-spark"
organization := "org.digitalpanda"

scalaVersion := "2.11.8"

val sparkVersion = "2.4.3"
val scalaTestVersion = "2.2.4"
val jUnitVersion = "4.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,

  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.6" // for visualization
)

libraryDependencies ++= Seq(
  "junit" % "junit" % jUnitVersion %  Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.1" % Test
)

/*
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % "2.6.0-cdh5.7.1" excludeAll ExclusionRule(organization = "javax.servlet"),
  "org.apache.spark" %% "spark-launcher" % "1.6.0-cdh5.7.1",
  "org.apache.spark" %% "spark-yarn" % "1.6.0-cdh5.7.1"
)
*/


parallelExecution in Test := false // So that tests are executed for each milestone, one after the other
