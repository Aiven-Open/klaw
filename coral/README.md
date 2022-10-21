# 🪸 Klaw's new frontend app

- Please be aware of our [Code of Conduct](../CODE_OF_CONDUCT.md) ❤️

## About

`/coral` contains a React app. It's the rewrite of the existing Klaw frontend. 

## Installation and usage 

ℹ️ Coral uses `pnpm` as a package manager. Read in their official documentation [how to install](https://pnpm.io/installation) it. 

- navigate to this directory
- run `pnpm install`
- run `pnpm dev` to start the frontend app in development mode

### Usage: How to run the app

ℹ️ You can see all our scripts in the [`package.json`](package.json).
You can also run `pnpm` in your console to get a list of all available scripts.

#### Scripts used and what they execute

- `build`: builds the frontend app for production
- `dev`: starts the frontend app for development
- `lint`: runs a code format check and if no error is found, lints the code. 
  - the linting script does not mutate your code. See [Linting and code formatting](#linting-and-code-formatting) for more info.
- `preview`: builds a preview production build _locally_
- `reformat`: runs the code formatter (prettier) and reformat all code
- `test`: runs all tests one time
- `test-dev`: runs all test tests related to changed files in a watch mode
- `typecheck`: runs the TypeScript compiler (tsc)

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

We use the component library of Aiven's design system:
  - 📃 [documentation](https://aiven-ds.netlify.app/)
  - the repository is open source, but `private` at the moment

As a rule, please don't use css classes from the design system. All styles should be created by using the existing components and their properties. 

__🔄 Work in progress related to styles__
- We plan adding css variables based on the design system's tokens.
- We plan having a custom theme for Klaw. This will be used instead of the Aiven theme.


## More detailed documentation

We provided more documentation on:

- Our commitment to [Accessibility](docs/accessibility.md)
- Detailed overview of the [Directory Structure](docs/directory-structure.md)
- Our thinking about [Docmentation](docs/documentation.md)
- More information about [Frontend Testing](docs/frontend-testing.md)
