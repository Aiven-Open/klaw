#!/bin/bash

# Declarations
version=2.5.0
core_lib=./core/target/klaw-${version}.jar
cluster_lib=./cluster-api/target/cluster-api-${version}.jar
core_config=./core/src/main/resources/application.properties
cluster_config=./cluster-api/src/main/resources/application.properties

# Start klaw
echo "Starting Klaw servers .."

echo "---------- Klaw Core ----------"

if ! [ -f "$core_lib" ]
then
  echo "$core_lib doesn't exist. Exiting .."
  exit 1
fi

if ps aux | grep 'core' | grep klaw
then
  echo "Core is already running" 2>&1
else
  echo "Starting core"
    nohup java -jar ${core_lib} --spring.config.location=${core_config} > /dev/null &
fi

echo "---------- Klaw Cluster api ----------"

if ! [ -f "$cluster_lib" ];
then
  echo "$cluster_lib doesn't exist. Exiting .."
  exit 1
fi

if ps aux | grep 'cluster-api' | grep 'java'
then
  echo "Cluster-api is already running" 2>&1
else
  echo "Starting cluster-api"
    nohup java -jar ${cluster_lib} --spring.config.location=${cluster_config} > /dev/null &
fi
