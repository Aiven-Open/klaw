# Development proxy server

We're running a proxy server to enable a convenient way of developing Coral locally without the need to connect a remote api.

## Table of content

- [Installation](#installation)
- [Commands and flags](#commands-and-flags)
  - [➡️ `pnpm dev:start`](#-pnpm-devstart)
  - [➡️ `pnpm dev:restart`](#-pnpm-devrestart)
  - [➡️ `pnpm:[start|restart]:testEnv`](#-pnpmstartrestarttestenv)
  - [➡️ `pnpm dev:stop`](#-pnpm-devstop)
  - [➡️ `pnpm dev:destroy`](#-pnpm-devdestroy)
  - [➡️ `pnpm run setup`](#-pnpm-run-setup)
  - [➡️ additional `--verbose` flag](#-additional---verbose-flag)
- [How it works](#how-it-works)
  - [These scripts are used by other scripts](#these-scripts-are-used-by-other-scripts)
  - [Files](#files)

## Installation

** ℹRequirements**

- Coral is set up - [Installation coral](../README.md)
- Docker is installed - [Get Docker](https://docs.docker.com/get-docker/)
- in this directory, run `pnpm install`

All scripts need to be run in this directory!

🙋‍♀️ **Your local development will always be available at [http://localhost:1337](http://localhost:1337)**

**⚠️ AUTHENTICATION AND LOGIN REDIRECT NOT WORKING**

At the moment, our proxy does **not** handle the redirect from backend for authenticating the user.

- If you access `https:localhost:1337` without being currently authorized, you will be redirected to the login page on port `:9097`. After a successful login, please go back to `https:localhost:1337`.

- If you experience trouble with api calls while using `https:localhost:1337`(\*), your authorization may have expired. Please go to `http://localhost:9097/login` to log in again. After a successful login, please go back to `https:localhost:1337`.

(\*) You'll see requests to `http://localhost:1337/api/getAuth` returning `302`, followed right after with calls to `http://localhost:9097/login` causing an error: "Access to fetch at 'http://localhost:9097/login' (redirected from 'http://localhost:1337/api/getAuth') from origin 'http://localhost:1337' has been blocked by CORS policy"

## Commands and flags

`pnpm dev` is our base command. It can be run with different flags. We provided alias commands for each flag, too, to make them more convenient to use.

### ➡️ `pnpm dev:start`

This is the alias for `pnpm dev --mode=start`.

Checks if Klaw core and Klaw cluster-api are running in the right ports in your docker container. If they are not, it will run a build and deploy job.

**Use `pnpm dev:start` when:**

- you run the docker containers for the first time
- there are changes in `/core` or `/cluster-api`, where you want a new build to run against the most current version

### ➡️ `pnpm dev:restart`

This is the alias for `pnpm dev --mode=restart`.

Checks if Klaw core and Klaw cluster-api are running in the right ports in your docker container. If they are not, it will trigger a new deployment, which will restart your container.

**Use `pnpm dev:restart` when:**

- you have a current version Klaw already in your container that you only want to restart

### ➡️ `pnpm:[start|restart]:testEnv`

This is the alias for `pnpm dev --mode=[start|restart] --testEnv=true`.

Runs the `pnpm dev` command with the mode flag start or restart and an additional flag "testEnv". When
`--testEnv=true` is set, we additionally check and, if necessary, set up a sandbox environment for Klaw.

**Use `pnpm:[start|restart]:testEnv` when:**

- you want to go through whole workflows, like for example requesting and approving a topic

### ➡️ `pnpm dev:stop`

This is the alias for `pnpm dev --mode=stop`.

This will stop all our docker containers. You can also do that in the Docker Dashboard if you're using a desktop app.

Note: You don't need to stop containers before you run `pnpm dev:start` for a new build.

**Use `pnpm dev:stop` when:**

You don't need the api anymore and want to stop the containers, and you probably don't need a new state next time you want to work with them again. You can start them up again next time with `pnpm dev:restart`.

### ➡️ `pnpm dev:destroy`

This is the alias for `pnpm dev --mode=destroy`.

This will stop and teardown all our docker containers. You can also do that in the Docker Dashboard if you're using a desktop app.

Note: You don't need to destroy containers before you run `pnpm dev:start` for a new build.

**Use `pnpm dev:destroy` when:**

- You don't need the api anymore and want to stop the containers, and you probably will need to use a new build next time anyway.

### ➡️ `pnpm run setup`

This is the alias for `pnpm dev --mode=all --testEnv=true`.

This will build Klaw, the cluster-api and a sandbox environment for klaw (container for zookeeper, kafka, schema-registry). You usually won't need that script, it is a convenient shortcut for the first setup.

### ➡️ additional `--verbose` flag

Adding this flag to any of the mentioned commands will run the proxy in "verbose" mode. We will print **all** requests that are handled in the terminal. This is very noisy, so not enabled by default. It can be a great way to debug in case something is not working like expecting.

## How it works

### These scripts are used by other scripts

- `_internal_use_proxy` starts the proxy.
- `_internal_use_start-coral` starts a vite local development server for Coral.
- `pnpm dev` runs the script `start-proxy-environment.sh` and is the base for all our `dev:` scripts. You _can_ use it with additional flags, but there is no need to.

### Files

- [`config.js`](config.js) contains all configuration for routes and our local ports.
- [`rules.js`](rules.js) util functions for handling rules defined in config.
- [`server.js`](server.js) set up and start of server as well as printing jobs.
- [`start-proxy-environment.sh`](start-proxy-environment.sh) is responsible for parsing the flags, running the correct docker scripts dependent on flags and start the proxy (running `pnpm _internal_use_proxy`) as well as coral (running `pnpm _internal_use_start-coral`).
