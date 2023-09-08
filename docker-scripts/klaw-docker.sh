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

# This is only for use in E2E tests. It skips testing to
# speed up the process. This assumes that the code it runs
# against is unit/integration tested.
buildForUITests () {
	echo "Build Klaw project binaries"
  mvn clean install -Dmaven.test.skip=true

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
  echo "Tear down container"
	docker-compose -f docker-scripts/docker-compose-klaw.yaml down
  docker-compose -f docker-scripts/docker-compose-testEnv.yaml down
  docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml down
  docker-compose -f docker-scripts/docker-compose-testEnv-v2.yaml down
}

stop() {
  echo "Stop container"
  docker-compose -f docker-scripts/docker-compose-klaw.yaml stop
  docker-compose -f docker-scripts/docker-compose-testEnv.yaml stop
  docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml stop
  docker-compose -f docker-scripts/docker-compose-testEnv-v2.yaml stop
}

deployDeveloperEnv() {
  echo `pwd`
  echo "Deploy developer Klaw"
  docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml up -d


  # Use 'docker-compose ps' to get information about running containers
  container_info=$(docker-compose -f docker-scripts/docker-compose-klaw-v2.yaml ps -q klaw-core)

  if [ -n "$container_info" ]; then
    container_address=$(docker port "$container_info" 9097)
    echo "Klaw container is running at $container_address"
  else
    echo "Klaw container not found or not running"
  fi
}


set echo off
usage () {

        echo "How to use klaw-docker:"
        echo "$0 --build will build Klaw and create klaw-core and klaw-cluster-api docker images in your local docker."
        echo "$0 --deploy will deploy klaw-core and klaw-cluster-api locally and make Klaw available at localhost:9097."
        echo "$0 --testEnv will deploy a local instance of Kafka at localhost:9092 to use with Klaw."
        echo "$0 --all will build all Klaw binaries, create Klaw docker images and deploy Klaw locally."
        echo "$0 --destroy will tear down containers running in docker."
        echo "$0 --dev-env will build and deploy a docker image that will run on Windows, Mac, or Linux for development."
        echo "$0 --dev-env-deploy will deploy without building again a docker image that will run on Windows, Mac, or Linux for development."
        echo "$0 --dev-kafka-env will deploy a Kafka and schema registry on Windows, Mac, or Linux."



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
  --stop)
    stop
    shift
    ;;
  --dev-env)
    build
    deployDeveloperEnv
    shift
    ;;
  --dev-ui-tests)
    buildForUITests
      if [ $? -eq 0 ]; then
        echo "⏳ Deploying Klaw for E2E tests..."
        deployDeveloperEnv
      else
        echo "Build failed. Skipping deployment."
        exit 1
      fi
    shift
    ;;
  --dev-env-deploy)
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

