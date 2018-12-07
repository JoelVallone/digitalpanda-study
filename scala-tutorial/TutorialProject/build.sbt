name := "TutorialProject"

version := "0.1"

scalaVersion := "2.12.6"

ivyConfigurations += Configurations.ScalaTool

resolvers += "Java.net Maven2 Repository" at "http://central.maven.org/maven2//"

// Test
libraryDependencies += "org.scalatest"    %% "scalatest"  % "3.0.5"   % "test"
libraryDependencies += "org.scalacheck"   %% "scalacheck" % "1.14.0"  % "test"