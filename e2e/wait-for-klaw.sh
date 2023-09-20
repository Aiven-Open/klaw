# This step may be brittle. The service in the container seems not to
# be immediately ready to accept connections, so the retry makes sure
# we're giving it (hopefully) enough time to be ready.
# @TODO figure out how to reuse this best with a variable host value
waitForKlawToBeReady() {
  host="http://localhost:9097"
  retries=5  # Number of retries
  interval=5  # Initial retry interval in seconds
  max_interval=30  # Max retry interval in seconds
  success=false

  echo "Check that Klaw is running on $host"

  for ((i = 0; i < retries; i++)); do
    if curl --fail --silent "$host"; then
    success=true
    echo "✅ Klaw is running on $host"
    break
  fi

  # Sleep before the next retry with exponential backoff
  sleep $interval
  interval=$((interval * 2))

  # Ensure the interval doesn't exceed the maximum
  if [ $interval -gt $max_interval ]; then
    interval=$max_interval
  fi
  done

  if [ "$success" = false ]; then
    echo "Klaw is not reachable 😭"
    exit 1
  fi
}

waitForKlawToBeReady