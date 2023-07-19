#!/bin/bash

DOCKER_FLAG=""
MODE=start
TEST_ENV=false
VERBOSE=false

check_port() {
  echo -e '\n\n🔎 Checking ports...'
  local port="$1"
  local name="$2"

  local status_code
  #  cluster-api returns a 403 and core a 302 when users are not signed in while checking
  status_code=$(curl -o /dev/null -s -w "%{http_code}" http://localhost:"$port")

  if [ "$status_code" = "200" ] || [ "$status_code" = "403" ] || [ "$status_code" = "302" ]; then
    echo -e "✅ Klaw $name is already running on localhost:$port"
  else
    echo -e "ℹ️Klaw $name is not running on localhost:$port, will start docker."
    return 1
  fi
}

parseArguments() {
  for arg in "$@"; do
    case $arg in
    -m=* | --mode=*)
      MODE="${arg#*=}"
      shift
      ;;
    -t==true* | --testEnv=true*)
      TEST_ENV="${arg#*=}"
      shift
      ;;
    -v | --verbose)
      VERBOSE="true"
      shift
      ;;
    esac
  done
}


checkDockerMode() {
  if [ "$MODE" = "start" ]; then
    DOCKER_FLAG="--all"
  elif [ "$MODE" = "restart" ]; then # if -a=null
    DOCKER_FLAG="--deploy"
  else
    echo "invalid flag: $MODE"
    exit 1
fi
}

startDocker() {
  DOCKER_COMMAND="docker-scripts/klaw-docker.sh"
  # Check if core is running on localhost:9097
  check_port 9097 "core" || {
    cd ../../
    "$DOCKER_COMMAND $DOCKER_FLAG"
    exit
  }

  # Check if cluster-api is running on localhost:9343
  check_port 9343 "cluster-api" || {
    cd ../../
    "$DOCKER_COMMAND $DOCKER_FLAG"
    exit
  }

  if [ "$TEST_ENV" = true ]; then
    # Check if zookeeper is running on localhost:2181
    check_port 2181 "zookeeper" || {
      cd ../../
      "$DOCKER_COMMAND" --testEnv
      exit
    }

    # Check if klaw-kafka is running on localhost:9092
    check_port 9092 "klaw-kafka" || {
      cd ../../
      "$DOCKER_COMMAND" --testEnv
      exit
    }

    # Check if klaw-schema-registry is running on localhost:8081
    check_port 8081 "klaw-schema-registry" || {
      cd ../../
      "$DOCKER_COMMAND" --testEnv
      exit
    }
  fi
}

startProxyServer() {
  echo -e "\n\n 🎬 Starting proxy server..."

  if [ "$VERBOSE" = true ]; then
    VERBOSE=true npm-run-all -l -p proxy start-coral
  else
    pnpm npm-run-all -l -p proxy start-coral
  fi
}

parseArguments "$@"
checkDockerMode
startDocker
startProxyServer