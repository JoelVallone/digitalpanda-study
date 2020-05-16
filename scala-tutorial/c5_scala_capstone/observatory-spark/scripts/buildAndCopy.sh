#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64;
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
PROJECT_FOLDER="${SCRIPT_FOLDER}/.."

cd ${PROJECT_FOLDER} && sbt -java-home $JAVA_HOME "set test in assembly := {}" clean assembly
scp ${PROJECT_FOLDER}/target/scala-2.11/observatory-spark-assembly-0.1.0-SNAPSHOT.jar panda-config@fanless1:/home/panda-config/panda-toolbox/state
