#!/bin/bash

WORKING_DIR="$(dirname "$0")"/..
cd $WORKING_DIR

# Declarations
version=2.7.0
core_lib=./core/target/klaw-${version}.jar
cluster_lib=./cluster-api/target/cluster-api-${version}.jar
core_config=./core/target/classes/application.properties
cluster_config=./cluster-api/target/classes/application.properties
core_log_file=core.log
clusterapi_log_file=cluster-api.log

# Start klaw
echo "Starting Klaw servers .."

echo "---------- Klaw Core ----------"

# Verify if config file exists
if ! [ -f "$core_lib" ]
then
  echo "$core_lib doesn't exist. Exiting .."
  exit 1
fi

# Verify if process already exists
if ps aux | grep 'core' | grep klaw
then
  echo "Core is already running" 2>&1
else
  echo "Starting core"
    nohup java -jar ${core_lib} --spring.config.location=${core_config} > ${core_log_file} &
    ps aux | grep 'core' | grep klaw
fi

echo "---------- Klaw Cluster api ----------"

# Verify if config file exists
if ! [ -f "$cluster_lib" ];
then
  echo "$cluster_lib doesn't exist. Exiting .."
  exit 1
fi

# Verify if process already exists
if ps aux | grep 'cluster-api' | grep 'java'
then
  echo "Cluster-api is already running" 2>&1
else
  echo "Starting cluster-api"
    nohup java -jar ${cluster_lib} --spring.config.location=${cluster_config} > ${clusterapi_log_file} &
    ps aux | grep 'cluster-api' | grep 'java'
fi

echo "Logging to $core_log_file $clusterapi_log_file"
