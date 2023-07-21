# Development with local Klaw

ℹ️ The proxy is a work in progress.

Please check out the [proxy README](../proxy/README.md) for more detailed information on how to use the proxy in your development process. You'll also find explanation and workaround for the things that are currently not working.

## Table of content

* [Basic setup](#basic-setup)
* [First setup](#first-setup)
* [How to run it](#how-to-run-it)

 
## Basic setup

ℹ️ environment settings are located in the file [`.env.local-api`](../../coral/.env.local-api).

** ℹRequirements**

- [node](https://nodejs.org/en/) needs to be installed <br/>
  -> see [nvmrc](../.nvmrc) or the `engines` definition in [package.json](../package.json) for version).
- Coral uses [pnpm](https://pnpm.io/) (version 7) as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) pnpm.

1. navigate to [`/coral`](../../coral)
2. run `pnpm install`
3. run `pnpm add-precommit` the first time you install the repository to set the custom directory for our pre commit hooks.
- go to directory [`coral/proxy`](../../coral/proxy)
- run `pnpm install` there, too
4. Run development:
   4.1. If you have not setup the docker environments, please follow [First setup]()
   4.2. If you already have a setup, run `pnmv dev:[start|restart]`
    - check out the [documentation](../../coral/proxy/README.md) for more scripts and information when to use them.
    - the proxy runs on [`http://localhost:1337`](http://localhost:1337)
  - ❗️ The correct redirect for login and authentication is **not** working yet. To authenticate yourself:
    - Go to your [local Klaw](http://localhost:9097/login)
    - Login with your credentials
    - After you've been sucessfully logged in, go back to the proxy -> [`http://localhost:1337`](http://localhost:1337)

🙋 The proxy and Klaw are running with `http`. Your browser may show you an error (SSL connection error), because it tries to automatically upgrade to https, without making an http request for security. You have to allow `localhost` to be accessed via http explicitly. 

ℹ️ When you're done, you can run either: 
- `pnpm dev:stop` to stop all containers in docker (enables a fast restart) 
- `pnpm dev:destroy` to tear down all containers in docker (you'll have to run `pnpm setup` or `pnpm:start` again next time you want to use them, so they get build again)

💡currently the proxy also does not redirect you to the login if your access expires. If you're getting related errors from your API, please login again like described 🙏


## First setup

1. Install Docker - [Get Docker](https://docs.docker.com/get-docker/)
2. Go to to directory [`coral/proxy`](../../coral/proxy)
3. run `pnpm install` if you haven't already
4. Run `ppm setup`
   `pnpm setup` will build and deploy your docker container for Klaw core, cluster api and a test environment for klaw (zookeeper, kafka, schema-registry).
5. ❗️Login - The correct redirect for login and authentication is **not** working in the proxy yet. To login:
    - Go to your [local Klaw](http://localhost:9097/login)
    - Login as superadmin with: `superadmin`, password `kwsuperadmin123$$` (see [application.properties](../../core/src/main/resources/application.properties))
    - Go back to the [proxy](http://localhost:1337) 
6. As superadmin, create one ore more users [proxy](http://localhost:1337/users)
    "User" and "superadmin" are roles that have authorization to different views and functionality. We're migrating the user views in Coral first, so you'll need to login to Coral with a "user" account to have access to all functionality.  
7. As superadmin, add a cluster and environment - you can follow our [official documentation](https://www.klaw-project.io/docs/getstarted)
8. Bootstrap server for Kafka cluster is `http://klaw-kafka:9092`, which is running in your docker
9. Bootstrap server SchemaRegistry is `klaw-schema-registry:8081`, which is running in your docker.
10. In [settings](`http://localhost:1337/serverConfig`), add the Cluster api `http://klaw-cluster-api:9343`, which is running in your docker. You can test the connection, using the button.
11. You are good to go! 🎉

## How to run it

If all requirements are met, and you've done your first setup, these are the scripts you can use in the directory `/proxy`:

- `pnpm dev:start` to build and start your docker container for klaw and cluster api
- `pnpm dev:start:testEnv` to build and start your docker container for klaw and cluster api as well as a sandbox with zookeeper, kafka and a schema-registry (note: these files are large!)
- `pnpm dev:restart` to restart you container
- `pnpm dev:restart:testEnv` to restart you container including sandbox
- `pnpm:stop` to stop your running containers
- `pnpm:destroy` to stop and teardown your running containers (will require you to run :start the next time again)

- Please check out the [proxy README](../proxy/README.md) for more detailed information and a deeper breakdown of all available scripts.