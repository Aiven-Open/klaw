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
# against is unit/integration tested. This is only aimed at
# pipeline/running E2E tests, which is why it's not
# listed in usage here!
buildForUITests () {
	echo "Build Klaw project binaries"
  mvn clean install -Dmaven.test.skip=true

	echo "Build klaw-core docker image"
  docker build -t klaw-core:latest --build-arg PROJECT_VERSION=$PROJECT_VERSION --build-arg JAR_FILE=./core/target/klaw-${PROJECT_VERSION}.jar -f core/Dockerfile .
  echo "Build klaw-cluster-api docker image"
  docker build -t klaw-cluster-api:latest --build-arg PROJECT_VERSION=$PROJECT_VERSION --build-arg JAR_FILE=./cluster-api/target/cluster-api-${PROJECT_VERSION}.jar  -f cluster-api/Dockerfile .
}

DOCKER_DIR=docker-scripts
ENV_FILE=$DOCKER_DIR/.env
SECRETS_FILE=$DOCKER_DIR/klaw-secrets.properties

# Creates the (git-ignored) local config files if they do not exist yet:
#  - .env with random values for the secrets Klaw needs to start
#  - klaw-secrets.properties from the example, for cluster credentials
# Existing files are never overwritten.
init () {
  if [ -f "$ENV_FILE" ]; then
    echo "$ENV_FILE already exists, leaving it unchanged."
  else
    SUPERADMIN_PASSWORD=$(openssl rand -base64 18)
    echo "KLAW_CLUSTERAPI_ACCESS_BASE64_SECRET=$(openssl rand -base64 32)" > "$ENV_FILE"
    echo "KLAW_SUPERADMIN_DEFAULT_PASSWORD='$SUPERADMIN_PASSWORD'" >> "$ENV_FILE"
    chmod 600 "$ENV_FILE"
    echo "Created $ENV_FILE with random values."
    echo "Log in to Klaw with user 'superadmin' and the password stored in $ENV_FILE."
  fi

  if [ -f "$SECRETS_FILE" ]; then
    echo "$SECRETS_FILE already exists, leaving it unchanged."
  else
    cp "$SECRETS_FILE.example" "$SECRETS_FILE"
    chmod 600 "$SECRETS_FILE"
    echo "Created $SECRETS_FILE from the example. Edit it to add your cluster credentials."
  fi

  # Truststores/keystores live outside the repository, so they can never be committed by accident.
  TRUSTSTORE_DIR="${KLAW_TRUSTSTORE_DIR:-$HOME/.klaw/truststore}"
  mkdir -p "$TRUSTSTORE_DIR"
  echo "Put your truststore/keystore files in $TRUSTSTORE_DIR (set KLAW_TRUSTSTORE_DIR in .env to use another folder)."
}

# Used by --deploy: Klaw must not start with missing or default secrets.
requireConfig () {
  if [ ! -f "$ENV_FILE" ] || [ ! -f "$SECRETS_FILE" ]; then
    echo "Missing $ENV_FILE or $SECRETS_FILE. Run '$0 --init' first, see docker-scripts/README.md."
    exit 1
  fi
}

deploy () {
	requireConfig
	echo `pwd`
	echo "Deploy Klaw"
	docker compose -f docker-scripts/docker-compose-klaw.yaml up -d
}

# Restarts the existing klaw-core and klaw-cluster-api containers (also if they are stopped).
# Does not rebuild or recreate them, so changes in the compose file are not applied: use --deploy for that.
restartKlaw () {
  requireConfig
  echo "Restart Klaw"
  docker compose -f docker-scripts/docker-compose-klaw.yaml restart
}

testEnv () {
	echo "Deploy Kafka"
  docker compose -f docker-scripts/docker-compose-testEnv.yaml up -d
}

destroy() {
  echo "Tear down container"
	docker compose -f docker-scripts/docker-compose-klaw.yaml down
  docker compose -f docker-scripts/docker-compose-testEnv.yaml down
}

stop() {
  echo "Stop container"
  docker compose -f docker-scripts/docker-compose-klaw.yaml stop
  docker compose -f docker-scripts/docker-compose-testEnv.yaml stop
}

# Only for the automated E2E tests (--dev-ui-tests). The E2E tests log in with this fixed,
# well-known password, so it must never be used for a real deployment.
# Exported variables take precedence over docker-scripts/.env.
useE2ETestCredentials () {
  export KLAW_CLUSTERAPI_ACCESS_BASE64_SECRET="dGhpcyBpcyBhIHNlY3JldCB0byBhY2Nlc3MgY2x1c3RlcmFwaQ=="
  export KLAW_SUPERADMIN_DEFAULT_PASSWORD='WelcomeToKlaw321@'
  if [ ! -f "$SECRETS_FILE" ]; then
    cp "$SECRETS_FILE.example" "$SECRETS_FILE"
  fi
}

deployDeveloperEnv() {
  # Developers get a working local setup without a separate step.
  # If E2E credentials were exported by useE2ETestCredentials, only the secrets file is needed.
  if [ -z "$KLAW_SUPERADMIN_DEFAULT_PASSWORD" ]; then
    init
  fi
  echo `pwd`
  echo "Deploy developer Klaw"
  docker compose -f docker-scripts/docker-compose-klaw.yaml up -d


  # Use 'docker compose ps' to get information about running containers
  container_info=$(docker compose -f docker-scripts/docker-compose-klaw.yaml ps -q klaw-core)

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
        echo "$0 --init will create the local, git-ignored config files (.env with random secrets and klaw-secrets.properties). Run this first."
        echo "$0 --build will build Klaw and create klaw-core and klaw-cluster-api docker images in your local docker."
        echo "$0 --deploy will deploy klaw-core and klaw-cluster-api locally and make Klaw available at localhost:9097."
        echo "$0 --testEnv will deploy a local instance of Kafka at localhost:9092 to use with Klaw."
        echo "$0 --all will build all Klaw binaries, create Klaw docker images and deploy Klaw locally."
        echo "$0 --restart will restart the existing klaw-core and klaw-cluster-api containers without rebuilding them."
        echo "$0 --stop will stop the containers."
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
	--init)
		init
		shift
		;;
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
	--restart)
		restartKlaw
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
    useE2ETestCredentials
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
    testEnv
    shift
    ;;
	*)
		usage
		;;
esac

