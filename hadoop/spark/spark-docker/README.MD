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

# Build the image

docker-image-tool.sh -r registry.hub.docker.com/blacelle -t ${imageTag} -f ./Dockerfile build
docker-image-tool.sh -r registry.hub.docker.com/blacelle -t ${imageTag} -f ./Dockerfile push

## Issues

### Authentication issue when pushing the image

> denied: requested access to the resource is denied

You can authenticate Docker with 'docker login registry.hub.docker.com --username blacelle'

### Walk inside the image

https://docs.docker.com/engine/reference/commandline/exec/

docker run --name ubuntu_bash --rm -i -t registry.hub.docker.com/blacelle/spark:scala211-spark24-hadoop33-v2 bash