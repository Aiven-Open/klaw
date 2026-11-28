# Development proxy server

The proxy lets you develop Coral locally without a remote API. It runs Klaw (core and cluster-api) in Docker, starts the
Coral dev server and puts both behind one address:

**Your local development is always available at <http://localhost:1337>.**

How Klaw runs in Docker (configuration, passwords, connecting clusters, data) is described in
[`docker-scripts/README.md`](../../docker-scripts/README.md). This page only covers the proxy.

## Requirements

- Coral is set up: [Installation Coral](../README.md)
- Docker is installed and running: [Get Docker](https://docs.docker.com/get-docker/)
- `pnpm install` has been run in this directory

All commands below are run in this directory.

## Commands

- `pnpm dev:start`: builds Klaw, starts it in Docker, then starts the proxy and Coral. **Fails if Klaw is already
  running.** The first run takes several minutes (Maven build) and creates `docker-scripts/.env` with a random
  password for the `superadmin` user.
- `pnpm dev:start:testEnv`: like `dev:start`, and also starts Kafka, ZooKeeper and a schema registry, so you can go
  through whole workflows like requesting and approving a topic.
- `pnpm dev:proxy`: starts only the proxy and Coral, for a Klaw that is already running. This is the command for
  everyday work on Coral. **Fails if Klaw is not running.**
- `pnpm dev:restart`: restarts the existing Klaw containers without rebuilding them, then starts the proxy and Coral.
  **Fails if the containers do not exist.**
- `pnpm dev:stop`: stops the Docker containers. Your data is kept.
- `pnpm dev:destroy`: stops and removes the Docker containers. The data stays in a Docker volume, see
  [Starting from scratch](../../docker-scripts/README.md#starting-from-scratch) to delete it too.

Add `--verbose` to print every request the proxy handles (very noisy, useful for debugging), for example
`pnpm dev --mode=proxy --verbose`.

The proxy and Coral stop with `Ctrl+C`. The Klaw containers keep running until you stop or destroy them.

**Changes in `/core` or `/cluster-api`:** to run them against a new build, run `pnpm dev:destroy` and then
`pnpm dev:start`.

## Login

The proxy does **not** handle the redirect from the backend for authenticating the user.

- Log in at <http://localhost:9097/login> as `superadmin`. The password is in `docker-scripts/.env`. Then go to
  <http://localhost:1337>.
- If API calls fail while you use <http://localhost:1337>, your session may have expired. Log in again at
  <http://localhost:9097/login>.

Symptom: requests to `http://localhost:1337/api/getAuth` return `302`, followed by a call to
`http://localhost:9097/login` that fails with a CORS error.

## How it works

- `pnpm dev` runs [`start-proxy-environment.sh`](start-proxy-environment.sh). It checks the Docker containers,
  calls [`docker-scripts/klaw-docker.sh`](../../docker-scripts/klaw-docker.sh) and starts the proxy and Coral. All
  `dev:` scripts are shortcuts for `pnpm dev --mode=<mode>`.
- `_internal_use_proxy` starts the proxy, `_internal_use_start-coral` starts the Vite dev server for Coral. Both are
  used by other scripts, you do not need to run them yourself.
- [`config.js`](config.js) contains the routes and local ports, [`rules.js`](rules.js) the helpers for the rules
  defined there, and [`server.js`](server.js) starts the server and prints the routes.
