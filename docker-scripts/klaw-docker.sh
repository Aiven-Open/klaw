#!/bin/bash

WORKING_DIR="$(dirname "$0")"/..
cd $WORKING_DIR

COMMAND=$1


PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

build () {

	echo "Build Klaw project binaries"
	mvn spotless:apply
	mvn clean install

	echo "Build klaw-core docker image"
  docker build -t klaw-core:latest --build-arg PROJECT_VERSION=$PROJECT_VERSION --build-arg JAR_FILE=./core/target/klaw-${PROJECT_VERSION}.jar -f core/Dockerfile .
	echo "Build klaw-cluster-api docker image"
  docker build -t klaw-cluster-api:latest --build-arg PROJECT_VERSION=$PROJECT_VERSION --build-arg JAR_FILE=./cluster-api/target/cluster-api-${PROJECT_VERSION}.jar  -f cluster-api/Dockerfile .

}

deploy () {
	echo `pwd`
	echo "Deploy Klaw"
	docker-compose -f docker-scripts/docker-compose-klaw.yaml up -d
}	

testEnv () {
	echo "Deploy Kafka"
  docker-compose -f docker-scripts/docker-compose-testEnv.yaml up -d
}

destroy() {
	docker-compose -f docker-scripts/docker-compose-klaw.yaml down
  docker-compose -f docker-scripts/docker-compose-testEnv.yaml down
  docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml down
  docker-compose -f docker-scripts/docker-compose-testEnv-v2.yaml down
}

deployDeveloperEnv() {
  	echo `pwd`
  	echo "Deploy developer Klaw"
  	docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml up -d
}

deployDeveloperTestEnv() {
  echo "Deploy developer Kafka"
  docker-compose -f docker-scripts/docker-compose-testEnv-v2.yaml up -d
}

set echo off
usage () {

        echo "How to use klaw-docker:"
        echo "$0 --build will build Klaw and create klaw-core and klaw-cluster-api docker images in your local docker."
        echo "$0 --deploy will deploy klaw-core and klaw-cluster-api locally and make klaw available at localhost:9097."
        echo "$0 --testEnv will deploy a local instance of kafka at localhost:9092 to use with klaw."
        echo "$0 --all will bulid all Klaw binaries, create Klaw docker images and deploy Klaw locally."
        echo "$0 --destroy will tear down containers running in docker."
        echo "$0 --dev-env will build and deploya docker image that will run on windows mac or linux for development."
        echo "$0 --dev-kafka-env will deploy a kafka and schema registry on windows, mac or linux"


}
set echo on
if [ $# -lt 1 ];
then
        usage
fi


case $COMMAND in 
	--all)
		build
		deploy
		shift
		;;
	--build)
		build
		shift
		;;
	--deploy)
		deploy
		shift
		;;
	--testEnv)
		testEnv
		shift
		;;
	--destroy)
		destroy
		shift
		;;
  --dev-env)
    build
    deployDeveloperEnv
    shift
    ;;
  --dev-kafka-env)
    deployDeveloperTestEnv
    shift
    ;;
	*)
		usage
		;;
esac

