#!/bin/bash

startDocker() {
  DOCKER_COMMAND="docker-scripts/klaw-docker.sh --dev-ui-tests"
  sh ../$DOCKER_COMMAND
  exit 0
}

startDocker