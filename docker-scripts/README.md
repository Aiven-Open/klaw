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


### How to know which version am I deploying?
If you use the klaw-docker.sh build and all commands this will build the local version of klaw and package that into a docker image ready to be deployed.
This way any changes code or config made will be packaged within the image.

### Deploying an official dockerhub release
If you want to deploy an official release of Klaw then you can update the docker-compose-klaw.yaml  'image'

klaw-core:latest -> aivenoy/klaw-core:latest

klaw-cluster-api:latest -> aivenoy/klaw-cluster-api:latest

If you choose to deploy Klaw this way you will need to externalise the configuration to make alterations to it as described below in 'How to Configure the docker images'


### Enabling HTTPS
A volume is created to store Klaw data this is where you can store your keystore and truststore for both enabling https but also for enabling secure connections between Klaw and Kafka.
The keystore and truststore should be copied to the klaw data volume so that it may be accessed at run time.

#### Where to find the Klaw docker volume
Run the following command with your containerId to find the location of your mount. ``docker inspect -f '{{ .Mounts }}' containerid``

#### Linux
linux volumes are normally in the same location.  
``/var/lib/docker/volumes/docker-scripts_klaw_data/_data```
##### Windows
Windows locations are also normally in the same location.  
``\\wsl$\docker-desktop-data\data\docker\volumes\docker-scripts_klaw_data\_data```



#### How to configure the docker images
Once the Keystores have been copied to the Klaw docker volume the keystore location is simply set to /klaw/client.keystore.p12 and /klaw/client.truststore.jks

This can be configured in two ways.

1. Configure the application.properties as normal and execute ```./klaw-docker.sh --all``` that will build and redeploy Klaw with the updated configuration settings
2. Configure the docker-compose-klaw.yaml environment variables to add to the environment settings and execute ```./klaw-docker.sh --deploy``` to redeploy the environmental changes and restart Klaw
 i. Here is an example of updating the docker-compose-klaw.yaml
```
    environment:
      KLAW_CLUSTERAPI_ACCESS_BASE64_SECRET: dGhpcyBpcyBhIHNlY3JldCB0byBhY2Nlc3MgY2x1c3RlcmFwaQ==
      SPRING_DATASOURCE_URL: "jdbc:h2:file:/klaw/klawprodb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"
      DEV1_KAFKASSL_KEYSTORE_LOCATION: "/klaw/client.keystore.p12"
      DEV1_KAFKASSL_KEYSTORE_PWD: "klaw1234"
      DEV1_KAFKASSL_KEY_PWD: "klaw1234"
      DEV1_KAFKASSL_KEYSTORE_TYPE: "pkcs12"
      DEV1_KAFKASSL_TRUSTSTORE_LOCATION: "/klaw/client.truststore.jks"
      DEV1_KAFKASSL_TRUSTSTORE_PWD: "klaw1234"
      DEV1_KAFKASSL_TRUSTSTORE_TYPE: "JKS"
      SERVER_SSL_KEYSTORE: "/klaw/client.keystore.p12"
      SERVER_SSL_TRUSTSTORE: "/klaw/client.truststore.jks"
      SERVER_SSL_KEYSTOREPASSWORD: "klaw1234"
      SERVER_SSL_KEYPASSWORD: "klaw1234"
      SERVER_SSL_TRUSTSTOREPASSWORD: "klaw1234"
      SERVER_SSL_KEYSTORETYPE: "pkcs12"
```
3. You can also externalize the application.properties to the volume and set the environment value in the docker-compose for it to use the local copy of application.properties.
   i. Ensure that the klaw.version property is updated correctly on the version copied over to the volume as this is normally updated during the build to keep the api versions in line with the pom version.
      Also ensure that the application.properties is renamed to a unique properties file name so you don't accidentally copy over the Core properties with the cluster properties and vice versa. 
````
    environment:
      KLAW_CLUSTERAPI_ACCESS_BASE64_SECRET: dGhpcyBpcyBhIHNlY3JldCB0byBhY2Nlc3MgY2x1c3RlcmFwaQ==
      SPRING_DATASOURCE_URL: "jdbc:h2:file:/klaw/klawprodb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"
      SPRING_CONFIG_LOCATION: "/klaw/klaw-application.properties"
````

#### How to use an existing H2 Database
If you already have a configured environment with users etc those can also be transferred over to the docker image by copying the klawprodb files into the docker volume.
It is recommended that any existing prodb files in the docker volume be backed up in case of any need to revert in the future.