#!/bin/bash

check_port() {
  local port="$1"
  local name="$2"

  local status_code
  #  cluster-api returns a 403 and core a 302 when users are not signed in while checking
  status_code=$(curl -o /dev/null -s -w "%{http_code}" http://localhost:"$port")

  if [ "$status_code" = "200" ] || [ "$status_code" = "403" ] || [ "$status_code" = "302" ]; then
    echo "✅ Klaw $name is already running on localhost:$port"
  else
    echo "ℹ️Klaw $name is not running on localhost:$port, will start docker."
    return 1
  fi
}

test_env=false

while getopts ":k-:" opt; do
  case "${opt}" in
    k)
      test_env=true
      ;;
    -)
      case "${OPTARG}" in
        testEnv)
          test_env=true
          ;;
        *)
          echo "Invalid option: --$OPTARG" >&2
          exit 1
          ;;
      esac
      ;;
    *)
      echo "Invalid option: -$opt" >&2
      exit 1
      ;;
  esac
done
shift $((OPTIND-1))


# Check if core is running on localhost:9097
check_port 9097 "core" || {
  cd ../../
  docker-scripts/klaw-docker.sh --all
  exit
}

# Check if cluster-api is running on localhost:9343
check_port 9343 "cluster-api" || {
  cd ../../
  docker-scripts/klaw-docker.sh --all
  exit
}

if [ "$test_env" = true ]; then
  # Check if core is running on localhost:9097
  check_port 2181 "zookeeper" || {
    cd ../../
    docker-scripts/klaw-docker.sh --testEnv
    exit
  }

  # Check if cluster-api is running on localhost:9343
  check_port 9092 "klaw-kafka" || {
    cd ../../
    docker-scripts/klaw-docker.sh --testEnv
    exit
  }

  # Check if cluster-api is running on localhost:9343
  check_port 8081 "klaw-schema-registry" || {
    cd ../../
    docker-scripts/klaw-docker.sh --testEnv
    exit
  }
fi


# All processes are running, exit without error
exit 0