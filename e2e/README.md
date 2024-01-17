# E2E tests

This is a work in progress, so a documentation will follow!

Most importantly, we need to make sure that the tests run on a Klaw instance with a clean and reproducible database which they don't at the moment.

## Current state

- to run tests on a local machine, use `pnpm test-local`
  - Please note that the tests are a proof-of-concept right now. We won't tackle adding tests before we have the setup of everything figured out, for example how to add consistent test data etc.
- to run test in development mode, use `pnpm test-local --ui`
- scripts with a `__` as prefixed are meant to be used internally
- the related github workflow is [end-to-end-tests.yaml](../.github/workflows/end-to-end-tests.yaml)
