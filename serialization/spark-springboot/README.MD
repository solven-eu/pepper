One goal here is to demonstrate how to start a Spark application in a Kubernetes cluster.

## Install Minikube

For easy local deployment, we can rely on Minikube:
https://minikube.sigs.k8s.io/docs/start/

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
    ./bin/spark-submit /
        --class cormoran.pepper.spark.run.pi.RunSparkPiAsSpringBoot /
        --master local[8] /
        ./target/spark-springboot-2.1-SNAPSHOT-exec.jar 100

# Deploy into a Kubernetes Cluster (Minikube)

https://spark.apache.org/docs/latest/running-on-kubernetes.html

    ./bin/docker-image-tool.sh -t benoitSparkPiSpringBoot build
    
# The following will open a local proxy so the actual Kubernetes cluster (hence not requiring to find its deeper configuration like its port)

Kubernetes needs the FatJar available on each executor node (plus the driver node). One may build a Docker image holding the FatJar.

Another option is to make the FatJar available through the Network. One easy way is to rely on an simple http server. For instance:

reference: https://superuser.com/questions/231080/extremely-simple-web-server-for-windows#comment359284_231106
reference: https://docs.docker.com/docker-for-mac/networking/#use-cases-and-workarounds

    python -m http.server
    
    kubectl proxy
    
    ./bin/spark-submit \
        --master k8s://http://localhost:8001 \
        --deploy-mode cluster \
        --name spark-pi \
        --class cormoran.pepper.spark.run.pi.RunSparkPiAsSpringBoot \
        --conf spark.executor.instances=5 \
        --conf spark.kubernetes.container.image=kube3-dtr-dev.fr.world.socgen/x202014/spark:v2 \
		--conf spark.kubernetes.file.upload.path=target/anythingTemporary \
        http://localhost:8000/target/spark-springboot-2.1-SNAPSHOT-exec.jar

## Kubernetes Helpers

### Monitoring:
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.4/aio/deploy/recommended.yaml
    
    http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login

### Access local images (e.g. to workaround proxy issues)

This will switch docker to Minikube environment

    https://stackoverflow.com/questions/40144138/pull-a-local-image-to-run-a-pod-in-kubernetes/40150867#40150867
    eval $(minikube docker-env)
    
Then you have to pull/build/tag the relevant image.
    
    docker pull datamechanics/spark:3.1.1-dm13
    
If this fails due to SSL (e.g. in some very tight environment/proxy), you may want to add '--insecure-registry' to Minikube start:

    minikube start --insecure-registry registry-1.docker.io:443

## Generate the list of artifacts to exclude:

 
    mvn dependency:tree -Pscala-2.11,spark-2.4,hadoop-2.10

    mvn dependency:tree -Pscala-2.12,spark-3.1,hadoop-3.2