#!/bin/bash
# Runs Klaw in Docker (see docker-scripts/README.md) and the Coral dev proxy.
# Usage and meaning of the modes: see README.md in this folder.

cd "$(dirname "$0")" || exit 1

DOCKER_SCRIPT="../../docker-scripts/klaw-docker.sh"
KLAW_CONTAINERS="klaw-core klaw-cluster-api"
TEST_CONTAINERS="klaw-zookeeper klaw-kafka klaw-schema-registry"

MODE=""
TEST_ENV=false
VERBOSE=false

fail() {
  echo -e "\n❌ $1\n" >&2
  exit 1
}

usage() {
  echo -e "\nUsage: pnpm dev --mode=<start|proxy|restart|stop|destroy> [--testEnv=true] [--verbose]"
  echo "- start:   build and start Klaw in Docker, then start the proxy and Coral. Fails if Klaw is already running."
  echo "- proxy:   start only the proxy and Coral, for a Klaw that is already running."
  echo "- restart: restart the existing Klaw containers (no rebuild), then start the proxy and Coral."
  echo "- stop:    stop the Docker containers."
  echo "- destroy: stop and remove the Docker containers."
  echo -e "\n"
}

parseArguments() {
  for arg in "$@"; do
    case $arg in
    -m=* | --mode=*)
      MODE="${arg#*=}"
      ;;
    --testEnv=true)
      TEST_ENV=true
      ;;
    -v | --verbose)
      VERBOSE=true
      ;;
    *)
      usage
      fail "Unknown argument: $arg"
      ;;
    esac
  done
}

requireDocker() {
  command -v docker >/dev/null 2>&1 || fail "Docker is not installed. Get it at https://docs.docker.com/get-docker/"
  docker info >/dev/null 2>&1 || fail "Docker is not running. Please start Docker and try again."
}

# true if a container with exactly this name is running
container_running() {
  [ -n "$(docker ps -q --filter "name=^$1\$")" ]
}

# true if a container with exactly this name exists, running or stopped
container_exists() {
  [ -n "$(docker ps -a -q --filter "name=^$1\$")" ]
}

startProxyServer() {
  echo -e "\n\n 🎬 Starting proxy server..."

  if [ "$VERBOSE" = true ]; then
    VERBOSE=true pnpm npm-run-all -l -p _internal_use_proxy _internal_use_start-coral
  else
    pnpm npm-run-all -l -p _internal_use_proxy _internal_use_start-coral
  fi
}

modeStart() {
  local containers="$KLAW_CONTAINERS"
  if [ "$TEST_ENV" = true ]; then
    containers="$containers $TEST_CONTAINERS"
  fi

  for container in $containers; do
    if container_running "$container"; then
      fail "$container is already running. Use 'pnpm dev:proxy' to work with it, 'pnpm dev:restart' to restart it, or 'pnpm dev:destroy' to remove it first."
    fi
  done

  "$DOCKER_SCRIPT" --dev-env || fail "Building or starting Klaw failed."

  if [ "$TEST_ENV" = true ]; then
    "$DOCKER_SCRIPT" --testEnv || fail "Starting the test Kafka failed."
  fi

  startProxyServer
}

modeProxy() {
  for container in $KLAW_CONTAINERS; do
    container_running "$container" || fail "$container is not running. Start Klaw first with 'pnpm dev:start' (or 'pnpm dev:restart' if the containers exist)."
  done

  startProxyServer
}

modeRestart() {
  for container in $KLAW_CONTAINERS; do
    container_exists "$container" || fail "$container does not exist, so there is nothing to restart. Use 'pnpm dev:start'."
  done

  "$DOCKER_SCRIPT" --restart || fail "Restarting Klaw failed."

  startProxyServer
}

parseArguments "$@"

case "$MODE" in
start | proxy | restart | stop | destroy)
  requireDocker
  ;;
*)
  usage
  fail "Please set a valid mode."
  ;;
esac

case "$MODE" in
start) modeStart ;;
proxy) modeProxy ;;
restart) modeRestart ;;
stop) "$DOCKER_SCRIPT" --stop ;;
destroy) "$DOCKER_SCRIPT" --destroy ;;
esac
