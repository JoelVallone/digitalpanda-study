* Dataset source: http://alaska.epfl.ch/files/scala-capstone-data.zip

* Copy dataset files into hdfs:
`hadoop fs -copyFromLocal -d /opt/panda-toolbox/ext/state/scala-capstone-data hdfs:///`

* Running code against cluster
```
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64; 
sbt -java-home $JAVA_HOME "set test in assembly := {}" clean assembly
```