* Dataset source: http://alaska.epfl.ch/files/scala-capstone-data.zip

* Copy dataset files into hdfs:
`hadoop fs -copyFromLocal -d /opt/panda-toolbox/ext/state/scala-capstone-data hdfs:///`

* Running code against cluster
```
./scripts/buildAndCopy.sh
toolbox
spark-submit \
    --class observatory.MainSpark \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m  \
    --executor-memory 2g \
    --executor-cores 1 \
    --queue default  \
    ${TOOLBOX_STATE}/observatory*.jar
```

* Kill spark application
```
yarn application -kill application_1589635410569_0008
```

* Example spark job submit from toolbox:
```
spark-submit \
    --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m  \
    --executor-memory 512m \
    --executor-cores 1 \
    --queue default  \
    ${SPARK_HOME}/examples/jars/spark-examples*.jar 100
```