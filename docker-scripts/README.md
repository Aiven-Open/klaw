## klaw-docker
klaw-docker.sh provides an easy way to build, deploy and test klaw in your local machine by utilising [docker](https://www.docker.com/) and [docker compose](https://docs.docker.com/compose/).

### Prerequisites
klaw-docker.sh builds the code locally and creates a local docker image for deployment.
So all the prerequisites for building klaw are still required (items 1-4 below). Docker and docker compose (items 5 & 6) are unique to klaw-docker.sh
1. Maven
2. Java
3. nodejs
4. pnpm
5. docker
6. docker compose

### How to use klaw-docker.sh
Execute klaw-docker.sh and the usage will appear.
```
How to use klaw-docker:
./klaw-docker.sh --build will build Klaw and create klaw-core and klaw-cluster-api docker images in your local docker.
./klaw-docker.sh --deploy will deploy klaw-core and klaw-cluster-api locally and make klaw available at localhost:9097.
./klaw-docker.sh --testEnv will deploy a local instance of kafka at localhost:9092 to use with klaw.
./klaw-docker.sh --all will bulid all Klaw binaries, create Klaw docker images and deploy Klaw locally.
./klaw-docker.sh --destroy will tear down containers running in docker.
```

* --build will compile your local code with any custom changes you have made and build a local docker image with that code and tag those images as `latest`.
* --deploy will deploy the klaw-core and cluster-api images tagged as `latest` to your local docker engine making the UI available locally at http://localhost:9097.
* --testEnv will deploy a Zookeeper, Kafka (localhost:9092) and schema registery (localhost:8081) into your local docker engine.  **These files are large**.
* --all will execute both the --build and --deploy options but will **not** execute the testEnv option.
* --destroy will tear down **both** Klaw containers and Kafka containers.

