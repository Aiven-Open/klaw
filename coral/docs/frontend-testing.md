# Frontend Testing

## Tools and setup for testing

This section offers an overview of our most frequently used tools.

## Quick links

- [Jest](about:blank#jest)
- [react-testing-library](about:blank#react-testing-library)
- [jest-dom-matcher](about:blank#jest-dom-matchers)
- [user-events](about:blank#user-events)

### Jest

🔗 [Jest - official documentation](https://jestjs.io/docs/)

We use [Jest](hhttps://jestjs.io/docs/) as test runner in Frontend in general and use the framework in our TypeScript tests. Jest is a testing framework with integrated test-runner for JavaScript. It can be used with Vanilla JS, ES6, TypeScript, Vue, React and so on.
.

### react-testing-library

🔗 [react-testing-library - official documentation](https://testing-library.com/docs/intro)

**⚠️ We use and need this library only for testing React components.**

`Dom testing library` is an opinionated library, which supports a specific way of writing test. It is well documented and supports us in having a consistent code base. It’s best used with jest-dom and the custom [jest-dom matchers](https://github.com/testing-library/jest-dom#readme). The combination of both leads to nice readable tests!

[react-testing-library](https://testing-library.com/docs/react-testing-library/intro) is a react-adapter for the [DOM testing-library](https://testing-library.com/docs/). 

### jest-dom matchers

🔗 [jest-dom matchers - official documentation](https://github.com/testing-library/jest-dom#readme)

To enable us to test the DOM, we use the standard [JSDOM](https://github.com/jsdom/jsdom) of Jest as environment. Additionally, we use the `@testing-library/jest-dom` library, which provides a set of custom jest matchers (e.g. `.toBeEnabled()` or `.toBeChecked()`) to extend the jest matcher. You should use them for your assertions. These will make your tests more declarative, clear to read and to maintain.

### user-events

🔗 [user-events - official documentation](https://github.com/testing-library/user-event/)

`user-event` is a higher-level abstraction of `fireEvent` from `DOM testing library` and tries to simulate the real events that would happen in the browser as the user interacts with our components. 


