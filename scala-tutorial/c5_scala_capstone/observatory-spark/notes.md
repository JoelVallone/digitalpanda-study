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
    --num-executors 4 \
    --executor-memory 1g \
    --executor-cores 1 \
    --queue default  \
    --conf spark.test1=aaa-spark.test1 \
    --conf spark.hadoop.test1=bbb-spark.hadoop.test1 \
    --conf spark.observatory.tile.fromYear=1976 \
    --conf spark.observatory.tile.toYear=1976 \
    --conf spark.observatory.tile.ZoomDepth=0 \
    --conf spark.observatory.tile.doSaveToLocalFS=true \
    --conf spark.observatory.tile.doSaveTilesToHDFS=true \
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