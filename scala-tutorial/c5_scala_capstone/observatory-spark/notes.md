* Dataset source: http://alaska.epfl.ch/files/scala-capstone-data.zip

* Copy dataset files into hdfs:
`hadoop fs -copyFromLocal -d /opt/panda-toolbox/ext/state/scala-capstone-data hdfs:///`

* Running code against cluster
  * Temperatures
    ```
    ./scripts/buildAndCopy.sh
    toolbox
    spark-submit \
        --class observatory.MainTemperaturesSpark \
        --master yarn \
        --deploy-mode cluster \
        --driver-memory 512m  \
        --num-executors 2 \
        --executor-memory 4g \
        --executor-cores 2 \
        --queue default  \
        --conf spark.observatory.tile.fromYear=1975 \
        --conf spark.observatory.tile.toYear=2015 \
        --conf spark.observatory.tile.ZoomDepth=3 \
        --conf spark.observatory.tile.doSaveToLocalFS=true \
        --conf spark.observatory.tile.doSaveTilesToHDFS=true \
        ${TOOLBOX_STATE}/observatory*.jar
    ```
   * Deviations
    ```
    ./scripts/buildAndCopy.sh
    toolbox
    spark-submit \
        --class observatory.MainDeviationsSpark \
        --master yarn \
        --deploy-mode cluster \
        --driver-memory 2g  \
        --num-executors 2 \
        --executor-memory 4g \
        --executor-cores 2 \
        --queue default  \
        --conf spark.observatory.deviation.normal.fromYear=1975 \
        --conf spark.observatory.deviation.normal.toYear=1989 \
        --conf spark.observatory.deviation.tile.fromYear=1990 \
        --conf spark.observatory.deviation.tile.toYear=2015 \
        --conf spark.observatory.deviation.tile.ZoomDepth=3 \
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