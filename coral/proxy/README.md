# Development with local node proxy

We're running a small node-proxy to enable a convenient way of developing Coral locally without the need to connect a remote api.

## Installation

** ℹRequirements**

- Coral is set up - [Installation coral](../README.md)
- Docker is installed - [Get Docker](https://docs.docker.com/get-docker/)
- in this directory, run `pnpm install`
- Vite mode for local api is created - [See documentation](../docs/development-with-local-klaw.md)

All scripts need to be run in this directory!

🙋‍♀️ **Your local development will always be available at [http://localhost:1337](http://localhost:1337)**

**⚠️ AUTHENTICATION AND LOGIN REDIRECT NOT WORKING**

At the moment, our proxy does **not** handle the redirect from backend for authenticating the user.

- If you access `https:localhost:1337` without being currently authorized, you will be redirected to the login page on port `:9097`. After a successful login, please go back to `https:localhost:1337`.

- If you experience trouble with api calls while using `https:localhost:1337`(\*), your authorization may have expired. Please go to `http://localhost:9097/login` to log in again. After a successful login, please go back to `https:localhost:1337`.

(\*) You'll see requests to `http://localhost:1337/api/getAuth` returning `302`, followed right after with calls to `http://localhost:9097/login` causing an error: "Access to fetch at 'http://localhost:9097/login' (redirected from 'http://localhost:1337/api/getAuth') from origin 'http://localhost:1337' has been blocked by CORS policy"

## Commands and flags

`pnpm dev` is our base command. It can be run with different flags. We provided different commands for each flag, too.

### ➡️ `pnpm dev --mode=start` or `pnpm dev:start`

Checks if Klaw core and Klaw cluster-api are running in the right ports in your docker container. If they are not, it will run a build and deploy job.

**Use `mode=start` when:**

- you run the docker container for the first time
- there where changes in `/core` or `/cluster-api`, where you want a new build to run against the most current version

### ➡️ `pnpm dev --mode=restart` or `pnpm dev:restart`

Checks if Klaw core and Klaw cluster-api are running in the right ports in your docker container. If they are not, it will trigger a new deploy, which will restart your container.

**Use `mode=restart` when:**

- you have a current version Klaw already in your container that you only want to restart

### ➡️ `pnpm dev --mode=[start|restart] --testEnv=true` or `pnpm:[start|restart]:testEnv`

Runs the `pnpm dev` command with the mode flag start or restart and an additional flag "testEnv". When `--testEnv=true` is set, we additionally check and, if necessary, setup a test environment for klaw (zookeeper, kafka, schema-registry).

**Use `mode=restart` when:**

- you want to go through whole workflows, like for example requesting and approving a topic

### ➡️ additional `--verbose` flag

Adding this flag to any of the mentioned commands will run the proxy in "verbose" mode. We will print **all** requests that are handled in the terminal. This is very noisy, so not enabled by default. It can be a great way to debug in case something is not working like expecting.

## How it works

### These scripts are used by other scripts

- `pnpm proxy` starts the proxy. It will automatically restart on changes.
- `pnpm start-coral` starts coral in development environment in local-api mode with HMR
- `pnpm dev` runs the script `start-proxy-environment.sh`

- `pnpm start-klaw-docker` runs a script that checks if core and cluster api are running on the expected ports. If not, it will run the docker script: [klaw-docker.sh --all](../../docker-scripts/klaw-docker.sh).
  - if the flag `--testEnv` is set here, we will also run the docker script [klaw-docker.sh --testEnv](../../docker-scripts/klaw-docker.sh).

### Files

- [`config.js`](config.js) contains all configuration for routes and our local ports.
- [`rules.js`](rules.js) util functions for handling rules defined in config.
- [`server.js`](server.js) set up and start of server as well as printing jobs.
- [`start-proxy-environment.sh`](start-proxy-environment.sh) is responsible for parsing the flags, running the correct docker scripts dependent on flags and start the proxy (running `pnpm proxy`) as well as coral (running `pnpm start-coral`).
