# 🪸 Klaw's new frontend app

- Please be aware of our [Code of Conduct](../CODE_OF_CONDUCT.md) ❤️

## Table of content

- [About](#about)
- [Installation and usage](#installation-and-usage)
- [Usage: How to run Coral inside the Klaw application](#usage-how-to-run-coral-inside-the-klaw-application)
  - [Usage: How to run Coral in development](#usage-how-to-run-coral-in-development)
    - [Scripts used and what they execute](#scripts-used-and-what-they-execute)
- [Tech stack](#tech-stack)
  - [App development](#app-development)
  - [Testing](#testing)
  - [Linting and code formatting](#linting-and-code-formatting)
- [Styling](#styling)
- [Documentation](#documentation)

## About

`/coral` contains a React app. It's the rewrite of the existing Klaw frontend.

## Installation and usage

** ℹRequirements**
- [node](https://nodejs.org/en/) needs to be installed (see [npmcr](.npmrc) for version).
- Coral uses [pnpm](https://pnpm.io/) (version 7) as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) pnpm. 

### Usage: How to run Coral inside the Klaw application

1. navigate to this directory
2. run `pnpm install`
3. run `make enable-coral-in-springboot` (see our [Makefile](Makefile)). This will enable Coral in Klaw. It also moves the Coral build files to the right directory.
4. go to the root directory and follow the [instructions on how to run Klaw](../README.md#Install)

➡️ Based on Springboot [application properties](https://github.com/aiven/klaw/blob/main/core/src/main/resources/application.properties#L5) configuration: 
- Klaw will run in `https://localhost:9097` if TLS is enabled
- Klaw will run in `http://localhost:9097` if TLS is not enabled

### Usage: How to run Coral in development

- navigate to this directory
- run `pnpm install`
- to start development mode, run:
  - `pnpm dev`sto start the frontend app for development in development mode **with remote API**
  - `pnpm dev-without-remote-api` to start the frontend app in development mode **without** api

ℹ️ **Using a remote API**
We recommend doing coral development with a remote API.
Please see our documentation: [Development with remote API](docs/development-with-remote-api.md).

ℹ️ **Developing without remote API**
If you want to run Coral without an API, you can do that, too.
Please see our documentation: [Mocking an API for development](docs/mock-api-for-development.md)

ℹ️ You can see all our scripts in the [`package.json`](package.json).
You can also run `pnpm` in your console to get a list of all available scripts.

#### Scripts used and what they execute

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
When we need custom styles - which should not be the case very often - we use [css modules](https://github.com/css-modules/css-modules). This enables us to add scoped css rules. Class names should be written in camelCases. 

#### 💁‍♀️ Special custom styles
-  We use styles in [`accessibility.modules.css`](./src/app/accessibility.module.css) to add css rules in order to improve accessibility. Add these styles with caution, since they are globally available. 
- In [`main.modules.css`](./src/app/main.module.css) are global styles that are needed as fundamentals.

**🔄 Work in progress related to styles**

- We plan to add css variables based on the design system's tokens.
- We plan to have a custom theme for Klaw. This will be used instead of the Aiven theme.

## Documentation

[More detailed documentation](docs/README.md) about `coral` can be found in the `docs` folder.
