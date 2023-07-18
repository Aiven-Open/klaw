# Development with local node proxy

We're running a small node-proxy to enable a convenient way of developing Coral locally without the need to connect a remote api.

## Installation and usage

** ℹRequirements**

- Coral is set up - [Installation coral](../README.md)
- Docker is installed - [Get Docker](https://docs.docker.com/get-docker/)
- in this directory, run `pnpm install`
- Vite mode for local api is created

## Scripts

All scripts need to be run in this directory!

🙋‍♀️ **Your local development will always be available at [http://localhost:1337](http://localhost:1337)**

### Scripts to use

#### `pnpm dev`

Most of the time, you will run `pnpm dev`.

This checks if Klaw core and Klaw cluster-api are running in the right ports in your docker container. If they are not, it will run the needed docker script.

If core and cluster-api are running, the development mode for coral will start. ###

**Background:**

- runs `start-klaw-docker` and, when that successfully finished, starts the proxy (`pnpm proxy`) as well as coral (`pnpm start-coral`). Both are staying in watch mode.
  see: [Internal scripts](./README.md#these-scripts-are-used-by-other-scripts)

#### `pnpm dev-with-test-env`

This script will run `pnpm dev` and additionally check and, if necessary, setup a test environment for klaw (zookeeper, kafka, schema-registry).

Run this script if you want to go through whole workflows, like for example requesting and approving a topic.

**Background:**

- runs `start-klaw-docker` with `--testEnv`. This will check not only for core and cluster api running on the right ports, but also for test envs (zookeeper, kafka, schema-registry). When that is successfully finished, it starts the proxy (`pnpm proxy`) as well as coral (`pnpm start-coral`). Both are staying in watch mode.
  see: [Internal scripts](./README.md#these-scripts-are-used-by-other-scripts)

#### `pnpm dev-verbose` and `pnpm dev-verbose-with-test-env`

This will run the proxy in verbose mode. We will print all requests that are handled in the terminal. This is very noisy, so not enabled by default. It can be a great way to debug in case something is not working like expecting.

## How it works

### These scripts are used by other scripts

- `pnpm proxy` starts the proxy. It will automatically restart on changes.
- `pnpm start-klaw-docker` runs a script that checks if core and cluster api are running on the expected ports. If not, it will run the docker script: [klaw-docker.sh --all](../../docker-scripts/klaw-docker.sh).
  - if the flag `--testEnv` is set here, we will also run the docker script [klaw-docker.sh --testEnv](../../docker-scripts/klaw-docker.sh).
- `pnpm start-coral` starts coral in development mode with HMR

### Files

- [`config.js`](config.js) contains all configuration for routes and our local ports.
- [`rules.js`](rules.js) util functions for handling rules defined in config.
- [`server.js`](server.js) set up and start of server as well as printing jobs.
