We copy here files coming from Spark and to be copied into a Kubernetes Image.

The Dockerfile build must refer to file relative to the folder where the build command is trigger. Hence, it is difficult to refer to both ${CURRENT_FOLDER} and ${SPARK_HOME}.

We exclude ./examples and ./data as they are not required for a standard application

It is important to keep the RELEASE file, else docker-image-build.sh behave differently (see 'function is_dev_build')