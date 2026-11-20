## Running Klaw with Docker

`klaw-docker.sh` builds, deploys and tears down Klaw on your machine with [docker](https://www.docker.com/) and
[docker compose](https://docs.docker.com/compose/). Klaw consists of two containers: **klaw-core** (UI and API, `http://localhost:9097`) and **klaw-cluster-api**.

All commands below are run from the root of the repository.

### Prerequisites

- `docker`, `docker compose` and `openssl`
- To build Klaw yourself (`--build`, `--all`): Maven, Java, Node.js and pnpm
- To use an official image instead of building: change `image:` in `docker-compose-klaw.yaml` to
  `aivenoy/klaw-core:<version>` and `aivenoy/klaw-cluster-api:<version>`, then run `--init` and `--deploy`

### Quick start

1. Create your local configuration (run once):

```
./docker-scripts/klaw-docker.sh --init
```

This creates these files and folders. None of them are in git, and they must never be committed:

- `docker-scripts/.env`: Random secrets Klaw needs to start, including the password of the `superadmin` user
- `docker-scripts/klaw-secrets.properties`: Credentials for your Kafka clusters (examples are commented out)
- `~/.klaw/truststore/`: Empty folder for truststore and keystore files

Existing files are not overwritten.

2. Build Klaw and start it:

```
./docker-scripts/klaw-docker.sh --all
```

3. Open <http://localhost:9097> and log in as `superadmin` with the password from `docker-scripts/.env`.

There are no default passwords. Klaw does not start if `.env` is missing.

> **The superadmin password is only used the first time Klaw starts with an empty database.** Changing it in `.env`
> later has no effect, because the user already exists in the database. Change the password in the Klaw UI, or
> start from scratch (see "Starting from scratch" below).

### Connecting a Kafka cluster

How to connect each cluster type (Aiven, Confluent Cloud, Apache Kafka over SSL or SASL...) is described on the
website: <https://www.klaw-project.io/docs/cluster-connectivity-setup/>. This section only covers what is specific
to the Docker setup.

1. **Add the cluster and an environment in the Klaw UI**, then note the **Cluster ID** on the Clusters page.
   The credentials of a cluster are found by their **prefix**: cluster name plus Cluster ID, in lowercase.
   Name `DEV_CLUSTER` with ID `1` becomes `dev_cluster1`.
2. **Credentials** go into `docker-scripts/klaw-secrets.properties`. The file contains commented examples. Uncomment
   what you need and replace `myprefix` with your prefix. The file is mounted into the klaw-cluster-api container as a
   Docker secret and is not part of the image.
3. **Truststore and keystore files** go into `${KLAW_TRUSTSTORE_DIR:-$HOME/.klaw/truststore}` (outside the repository). 
   By default, this is `~/.klaw/truststore/` on the host machine. Inside the container, the files are available as 
   `/truststore/<file name>`. Use the container path (e.g., `/truststore/client.truststore.jks`) in `klaw-secrets.properties`.
   **Note:** If the default path does not work (e.g., on Docker Desktop for Mac/Windows), set `KLAW_TRUSTSTORE_DIR` in 
   `docker-scripts/.env` to the **absolute host path** of your truststore folder. For example:
   ```bash
   KLAW_TRUSTSTORE_DIR=/Users/your-username/.klaw/truststore
   ```
4. **Restart cluster-api** after every change to `klaw-secrets.properties`, the truststore files, or `KLAW_TRUSTSTORE_DIR` in `.env`.
   Spring reads them only at start-up:

```
docker restart klaw-cluster-api
docker exec klaw-cluster-api ls -la /truststore /run/secrets
```

The listing must show your truststore file and `klaw-secrets.properties`.

#### Common errors if the cluster is offline

Look at the recent errors of cluster-api:

```
docker logs --since 5m klaw-cluster-api 2>&1 | grep -i -E "error|exception|caused by" | tail -20
```

- `NoSuchFileException: /truststore/...`: the file is not in `~/.klaw/truststore/`, or the path in
  `klaw-secrets.properties` does not match its name.
- `PKIX path building failed`: the truststore does not contain the CA that signed the server certificate. A CA file
  can hold several certificates (e.g., Aiven's `ca.pem` includes both the old and new generation CAs), 
  and `keytool -import` imports only the first one from a file. To fix this, split the file and import all certificates:
  ```bash
  awk '/BEGIN CERTIFICATE/{n++} {print > ("ca-" n ".pem")}' ca.pem
  keytool -import -noprompt -file ca-1.pem -alias ca-1 -keystore truststore.jks -storepass <password>
  keytool -import -noprompt -file ca-2.pem -alias ca-2 -keystore truststore.jks -storepass <password>
  ```
  Replace `<password>` with your truststore password.
- `Keystore was tampered with, or password was incorrect`: `truststore.pwd` in `klaw-secrets.properties` is not the
  password of the truststore.
- `Invalid keystore format`: set `<prefix>.kafkassl.truststore.type=PKCS12` (or `PEM`) to match the file.
- `SaslAuthenticationException` or "Authentication failed": wrong user or password, or the prefix does not match
  `<cluster name><cluster id>` in lowercase.
- `TimeoutException`, `UnknownHostException` or connection refused: wrong bootstrap server or port, the cluster is
  not running, or a firewall or allow-list blocks your machine.

### Where your data lives

- **Registered clusters, users, teams and requests** are stored in an H2 database in the Docker volume `klaw_data`
  (`docker-scripts_klaw_data`). A new machine starts with an empty database, so you have to register your clusters
  again, or copy the volume contents (the `klawprodb*` files) from the old machine.
- **Credentials** are only in `.env`, `klaw-secrets.properties` and your truststore folder. Back them up separately.

To find the volume on disk, run `docker volume inspect docker-scripts_klaw_data`.

### Starting from scratch

`--destroy` removes the containers but keeps the volume, so your data survives. To delete everything, including the
database, users and registered clusters:

```
./docker-scripts/klaw-docker.sh --destroy
docker volume rm docker-scripts_klaw_data
rm -f docker-scripts/.env docker-scripts/klaw-secrets.properties ~/.klaw/truststore/*
```

Run `--destroy` first: it needs `.env`. Then start again with `--init` as in the Quick start.

### Commands

```
./docker-scripts/klaw-docker.sh --init            create .env, klaw-secrets.properties and the truststore folder (run once)
./docker-scripts/klaw-docker.sh --build           build Klaw and the klaw-core and klaw-cluster-api images
./docker-scripts/klaw-docker.sh --deploy          start Klaw at http://localhost:9097
./docker-scripts/klaw-docker.sh --all             --build and --deploy
./docker-scripts/klaw-docker.sh --stop            stop the containers
./docker-scripts/klaw-docker.sh --destroy         remove the containers (the volume with your data is kept)
./docker-scripts/klaw-docker.sh --testEnv         start a local Kafka (localhost:9092) and Schema Registry (localhost:8081) for testing
./docker-scripts/klaw-docker.sh --dev-env         developer: build and deploy, creates .env automatically if missing
./docker-scripts/klaw-docker.sh --dev-env-deploy  developer: deploy without building
```

`--dev-ui-tests` is used by the automated end-to-end tests. It uses fixed, well-known credentials and must not be used
for anything else.

### Security notes

- Ports are published on `127.0.0.1` only. To reach Klaw from other machines, put a reverse proxy with HTTPS in front of it.
- `klaw-secrets.properties` is mounted into klaw-cluster-api as a Docker secret (`/run/secrets/`) and loaded with
  Spring's `spring.config.import`. It does not appear in `docker inspect`, unlike environment variables.
- To enable HTTPS on Klaw itself, see the `server.ssl.*` properties in the application properties of each module.
