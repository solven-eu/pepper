This project enables building Docker images compatible with Kubernetes, and with custom Spark+Hadoop versions

# Prepare the jars
	cd spark/spark-docker/
	
mvn clean assembly:single -Pscala-2.11,spark-2.4,hadoop-3.3
export imageTag=scala211-spark24-hadoop33-v2

For Powershell:

    $imageTag = "scala211-spark24-hadoop33-v2"
    
# Prepare the environment

We fake a SPARK_HOME inside the target folder. It enables a custom set of jars. However, it requires duplicating the other ressources from an actual SPARK_HOME (as duplicated in 'src/main/resources/spark-3.1.2').

We map SPARK_HOME to our custom build:

    export SPARK_HOME=target/scala2.11-spark2.4-hadoop3.3

This seems not useful anymore

    //export PATH=${PATH}:${SPARK_HOME}/bin

# Build the image

export HTTP_PROXY=http://x119400:XXX@proxy-sgt.si.socgen:8080
docker-image-tool.sh -r harbor.core.euw.gbis.sg-azure.com/dlhybrid/benoit -t ${imageTag} -f ./Dockerfile -b HTTP_PROXY=${HTTP_PROXY} build
docker-image-tool.sh -r harbor.core.euw.gbis.sg-azure.com/dlhybrid/benoit -t ${imageTag} -f ./Dockerfile -b HTTP_PROXY=${HTTP_PROXY} push

TODO WARNING

This way of building a docker image will leak the secret to logs. One can follow the following best practise: 
https://vsupalov.com/better-docker-build-secrets/#a-simpler-way

## Issues

### Proxy configured to localhost

> Could not connect to localhost:3128 (127.0.0.1)

We should switch HTTP_PROXY from 'localhost:3128' to 'http://x119400:XXX@proxy-sgt.si.socgen:8080'

### Authentication issue when pushing the image

> denied: requested access to the resource is denied

You can authenticate Docker with 'docker login harbor.core.euw.gbis.sg-azure.com --username dlhybrid'

### Walk inside the image

https://docs.docker.com/engine/reference/commandline/exec/

docker run --name ubuntu_bash --rm -i -t harbor.core.euw.gbis.sg-azure.com/dlhybrid/benoit/spark:scala211-spark24-hadoop33-v2 bash