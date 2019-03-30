name := "TutorialProject"

version := "0.1"

scalaVersion := "2.12.6"

ivyConfigurations += Configurations.ScalaTool

resolvers += "Java.net Maven2 Repository" at "http://central.maven.org/maven2//"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"