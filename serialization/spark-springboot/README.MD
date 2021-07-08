One goal here is to demonstrate how to start a Spark application in a Kubernetes cluster.

For easy local deployment, we can rely on Minikube:
https://minikube.sigs.k8s.io/docs/start/

mvn install

ls ./target/spark-springboot-2.1-SNAPSHOT-exec.jar

## Download Spark

You can clone Spark source-code: Unclear how much you need to compile it for now

    git clone git@github.com:apache/spark.git
    cd spark

Else, download Spark from:
https://spark.apache.org/downloads.html

## Setup Environment

For Windows:
export SPARK_HOME=C:\\...\\spark-3.1.2-bin-hadoop3.2
export PATH=%PATH%;SPARK_HOME\\bin

Without this: https://stackoverflow.com/questions/50435286/spark-installation-error-could-not-find-or-load-main-class-org-apache-spark-l

## Configure environment

If you see:

    log4j:WARN Please initialize the log4j system properly.

It means you haven't configure Log4J correctly:

You can setup your configuration in a file named $(SPARK_HOME)/conf/log4j.properties, based on $(SPARK_HOME)/conf/log4j.properties.template

reference: https://stackoverflow.com/questions/35639159/running-apache-spark-log4jwarn-please-initialize-the-log4j-system-properly

## Tutorial for executing Spark

https://spark.apache.org/docs/latest/running-on-kubernetes.html
https://spark.apache.org/docs/2.1.1/submitting-applications.html


On Windows, BEWARE of executing spark-submit from a Git bash: https://stackoverflow.com/questions/65500297/setting-up-spark-shell-in-git-bash-on-windows
You'd better launching from Powershell:

     ./bin/spark-submit --class org.apache.spark.examples.SparkPi --master local[8] https://docs.azuredatabricks.net/_static/libs/SparkPi-assembly-0.1.jar 100

# Run our sample SpringBoot application in local

    cd ../pepper/serialization/spark-springboot
    mvn install
    spark-submit --class cormoran.pepper.spark.run.pi.RunSparkPiAsSpringBoot --master local[8] ./target/spark-springboot-2.1-SNAPSHOT-exec.jar 100