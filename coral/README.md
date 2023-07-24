# 🪸 Klaw's new frontend app

- Please be aware of our [Code of Conduct](../CODE_OF_CONDUCT.md) ❤️

## Table of content

* [About](#about)
* [Installation and usage](#installation-and-usage)
  + [First setup](#first-setup)
  + [Usage: How to run Coral in development](#usage-how-to-run-coral-in-development)
    - [Local development _without_ connecting an api](#local-development-without-connecting-an-api)
      * [What it does](#what-it-does)
      * [When you want to use this](#when-you-want-to-use-this)
      * [How to do it](#how-to-do-it)
    - [Local development with remote API](#local-development-with-remote-api)
      * [What it does](#what-it-does-1)
      * [When you want to use this](#when-you-want-to-use-this-1)
      * [How to do it](#how-to-do-it-1)
    - [Local development with local API](#local-development-with-local-api)
      * [What it does](#what-it-does-2)
      * [When you want to use this](#when-you-want-to-use-this-2)
      * [How to do it](#how-to-do-it-2)
  + [Build for deployment](#build-for-deployment)
    - [What it does](#what-it-does-3)
    - [When you want to use this](#when-you-want-to-use-this-3)
    - [How to](#how-to)
* [Scripts used and what they execute](#scripts-used-and-what-they-execute)
* [Tech stack](#tech-stack)
  + [App development](#app-development)
  + [Testing](#testing)
  + [Linting and code formatting](#linting-and-code-formatting)
* [Styling](#styling)
  + [Custom styles](#custom-styles)
    - [💁‍♀️ Special custom styles](#-special-custom-styles)
* [Documentation](#documentation)


## About

`/coral` contains a React app. It's the rewrite of the existing Klaw frontend.

## Installation and usage

### First setup

This is the setup you every time. You can find the different ways how to run the local development process below. 

** ℹRequirements**

- [node](https://nodejs.org/en/) needs to be installed <br/> 
    -> see [nvmrc](.nvmrc) or the `engines` definition in [package.json](package.json) for version).
- Coral uses [pnpm](https://pnpm.io/) (version 7) as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) pnpm. 

1. navigate to this directory
2. run `pnpm install`
3. run `pnpm add-precommit` the first time you install the repository to set the custom directory for our pre commit hooks.

### Usage: How to run Coral in development

You have different ways of working on Coral in the development process:

#### Local development _without_ connecting an api

Note: We don't recommend using this way, except in a few cases. Development against a real Klaw API will yield in better developer confidence of the functionality and developer experience compared to using a mocked API. 

#####  What it does
This will run the vite development server. Coral will run, but there is no API that it can consume. You will have to add a mocked response for authentication to be able to see the UI. In case you need data to work with, you'll have to mock API responses. 

##### When you want to use this
- You want to do small changes on areas that are not dealing with data from the API.
- You're want to do a small task first without having to setup a remote-api or docker.

##### How to do it

- Please follow our [Development without API guide](./docs/development-witout-api.md)


#### Local development with remote API

#####  What it does
This will run the vite development. It will use a external address as API source. For this, you need to have Klaw deployed to be able to connect to its API. The remote API can be a shared staging server, or even a production system.

##### When you want to use this
- You have access to a deployed instance of Klaw and only want to do Frontend changes.
- You don't want to deal with setting up docker and running Klaw locally.

##### How to do it

- Please follow our [Development with remote API guide](./docs/development-with-remote-api.md)


#### Local development with local API

⚠️ Note: This is still a work in progress, but should already allow Frontend developer to access a full development environment without needing to connect to a remote API.

#####  What it does
This will run a small node proxy server. It will serve Coral via vite development server locally and Klaw from a docker container, which will act as the remote API. With this, we want to enable contributors to have the better developer experience and confidence without the need to have a deployed instance of Klaw running and use this as remote API.

##### When you want to use this
- You have don't have access to a deployed instance of Klaw or don't want to use that.
- You want a local environment for Klaw while also benefiting from the vite development server (with hot module replacement)

##### How to do it

- Please follow our [Development with local API guide](./docs/development-with-local-klaw.md)

### Build for deployment

####  What it does
Builds the Klaw application locally, bundling the Coral frontend with it.

#### When you want to use this
You can see how your local state of Coral will look and behave like in the production-ready build. This can be useful for manual testing and exploring.

#### How to

1. navigate to this directory
2. run `pnpm install`
3. go to the root directory and follow the [instructions on how to run Klaw](../README.md#Install)

➡️ Based on Springboot [application properties](https://github.com/aiven/klaw/blob/main/core/src/main/resources/application.properties#L5) configuration:

- Klaw will run in `https://localhost:9097` if TLS is enabled
- Klaw will run in `http://localhost:9097` if TLS is not enabled


## Scripts used and what they execute

ℹ️ You can see all our scripts in the [`package.json`](package.json).
You can also run `pnpm` in your console to get a list of all available scripts.

- `build`: builds the frontend app for production
- `dev`: starts the frontend app for development in development mode **with remote API**
- `dev-without-api` starts the frontend app in development mode **without** api
- `lint`: runs a code format check and if no error is found, lints the code.
  - the linting script does not mutate your code. See [Linting and code formatting](#linting-and-code-formatting) for more info.
- `preview`: builds a preview production build _locally_
- `reformat`: runs the code formatter (prettier) and reformat all code
- `test-dev`: runs all test tests related to changed files in a watch mode
- `test`: runs all tests one time
- `tsc`: runs the TypeScript compiler

ℹ️ We are using a custom hook path for enabling pre-commit hooks. This path is set in the local git config when running `pnpm install`. See script `pnpm:devPreinstall`.


## Tech stack

### App development

- TypeScript - 📃 [documentation](https://www.typescriptlang.org/) | 🐙 [repository](https://github.com/microsoft/TypeScript)
- React - 📃 [documentation](https://reactjs.org/docs/getting-started.html) | 🐙 [repository](https://github.com/facebook/react/)
- Vite - 📃 [documentation](https://vitejs.dev/guide/) | 🐙 [repository](https://github.com/vitejs/vite)

### Testing

- Jest - 📃 [documentation](https://jestjs.io/docs/getting-started) | 🐙 [repository](https://github.com/facebook/jest)
- React Testing Library - 📃 [documentation](https://testing-library.com/docs/react-testing-library/intro/) | 🐙 [repository](https://github.com/testing-library/react-testing-library)

📃 You can find more detailed information about testing in our docs for [Frontend Testing](docs/frontend-testing.md).

### Linting and code formatting

How we keep our app's codebase looking consistent and nice 💅🏼

- [Prettier](https://prettier.io/) for code formatting
- [ESlint](https://eslint.org/) and various plugins for linting

The script `lint` runs a prettier check and eslint after. It does not mutate your code in any way. If you want to format your code or let eslint fix it for you, you can run:

1. First: `pnpm reformat` (prettier formatting)
2. After that: `pnpm eslint --fix` (eslint in fix mode)

ℹ️ It's convenient to let prettier and eslint auto-format your code "on save" by your IDE or editor.

## Styling

Coral uses the component library of Aiven's Aquarium design system:

- 📃 [documentation component library](https://aquarium-library.aiven.io/)
- 📙 [more information about Aquarium DS](https://aquarium.aiven.io/43ae72f19/p/560f47-aquarium-ds)
- the repository is open source, but `private` at the moment

As a rule, please don't use css classes from the design system. All styles should be created by using the existing components and their properties.

### Custom styles
When we need custom styles - which should not be the case very often - we use [css modules](https://github.com/css-modules/css-modules). This enables us to add scoped css rules. Class names should be written in camelCase. 

#### 💁‍♀️ Special custom styles
-  We use styles in [`accessibility.modules.css`](./src/app/accessibility.module.css) to add css rules in order to improve accessibility. Add these styles with caution, since they are globally available. 
- In [`main.modules.css`](./src/app/main.module.css) are global styles that are needed as fundamentals.

**🔄 Work in progress related to styles**

- We plan to add css variables based on the design system's tokens.
- We plan to have a custom theme for Klaw. This will be used instead of the Aiven theme.

## Documentation

[More detailed documentation](docs/README.md) about `coral` can be found in the `docs` folder.
