# Frontend Testing

## Table of content

- [Tools and setup for testing](#tools-and-setup-for-testing)
    * [Jest](#jest)
    * [react-testing-library](#react-testing-library)
    * [jest-dom matchers](#jest-dom-matchers)
    * [user-events](#user-events)
- [Frontend testing approach](#frontend-testing-approach)
    * [General rules](#general-rules)
    * [Goals of testing approach](#goals-of-testing-approach)
    * [How to test](#how-to-test)
        + [How to get the best out of our libs](#how-to-get-the-best-out-of-our-libs)
            - [Use `screen` when possible](#use--screen--when-possible)
            - [follow the [recommended priority how to use queries](https://testing-library.com/docs/queries/about/#priority)](#follow-the--recommended-priority-how-to-use-queries--https---testing-librarycom-docs-queries-about--priority-)
            - [make use of the [jest-dom matcher](https://github.com/testing-library/jest-dom)](#make-use-of-the--jest-dom-matcher--https---githubcom-testing-library-jest-dom-)
                * [Example and benefits of jest-dom matcher](#example-and-benefits-of-jest-dom-matcher)
        + [How to structure test files](#how-to-structure-test-files)
        + [How to group tests meaningful](#how-to-group-tests-meaningful)
            - [Example: Grouping tests that check the default markup is rendered](#example--grouping-tests-that-check-the-default-markup-is-rendered)
            - [Example: Grouping tests that test user interaction](#example--grouping-tests-that-test-user-interaction)
    * [How to write test descriptions](#how-to-write-test-descriptions)
        + [General tip](#general-tip)
        + [Recommended rules](#recommended-rules)
            - [1. Use uncomplicated language](#1-use-uncomplicated-language)
            - [3. Use simple present tense for expected results](#3-use-simple-present-tense-for-expected-results)
            - [4. Use direct and unambiguous language](#4-use-direct-and-unambiguous-language)
            - [5. Use user-based language for React component tests](#5-use-user-based-language-for-react-component-tests)


## Tools and setup for testing

This section offers an overview of our most used tools.

**Quick links**

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


## Frontend testing approach

### General rules

- We test all code.
- Test files live right beside the code they test.
- Extract functionality in pure TypeScript files where possible. They are faster and less complicated to test than React components.


### Goals of testing approach

Our tests...
- 🧑 confirm code works __as expected for users__.
  - User is the consumer of the web app.
  - User is a developer in the code base.
- ✅ confirm behavior and functionality __for their component__.
  - Test every component explicitly.
- 🔎 help us to locate an error fast.
- 📕add __readability__ to our code base.
  - A dev can understand what a component does by looking only at the test.

### How to test

#### How to get the best out of our libs

##### Use `screen` when possible

The `screen` object has every query (method used to _query_ for elements) pre-bound to document.body. It is used like this:

```tsx
import { screen } from '@testing-library/react'
// ...
expect(screen.getByRole("button", {name: "click" })).toBeEnabled()
```

We recommend to use screen where it is possible. It helps developer experience because you can see which queries are available on it. There are very few cases where you would need to use the `container` element returned from `render`. 

##### follow the [recommended priority how to use queries](https://testing-library.com/docs/queries/about/#priority)

A great benefit is that this can help us making our markup more accessible. If you can't use the recommended query, you should check if there is a reason for that. 

**Example**: There is a alert and you can't query it other than with looking for the text? That means assistive technology also has no information that this is a warning. Changing that element to have an [alert role](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles/alert_role) enables you to use the recommended queries. And it makes the content more accessible!

**💡 Tip to remember the right query**

testing-library has an [experimental config](https://testing-library.com/docs/dom-testing-library/api-configuration/#throwsuggestions-experimental). It can throw an error if there is a better query available. For example when using `.getByText("click")` instead of `.getByRole('button', { name: "click" })`.

It also suggests a better available query! (it suggests regex in name, so, it’s not perfect 😅, but still!)

You can make use of that by adding at top of your test file:

```
configure({
throwSuggestions: true
})
```

This adds the config only to your test file. No others are not affected! The config is making mistakes and still experimental, so we won't use it in a general config. It's up to you if you want to remove it in your test file after writing the tests. 

⚠️ Note: it only suggests a better query if the element in the component is semantically correct / accessible. It only solve something on the test side (using better queries to make test implicitly test more important things), not on the code side.

##### make use of the [jest-dom matcher](https://github.com/testing-library/jest-dom)

These matchers not only make the tests more declarative, they also support to write test in a natural feeling and user-centered way. The library is actively worked, so make sure to check out their documentation to find useful matchers for specific use-cases!

It’s a good practice to choose a **precise** matcher. For example `.toBeEnabled()` is preferred to `.toBeVisible()` if testing a (non-disabled) button in your React component.


###### Example and benefits of jest-dom matcher

(in combination with the queries)

```
it('shows a checkbox to confirm user has read the text', () => {
const checkbox = screen.getByRole('checkbox', { name: 'Confirm that I read the text' })

expect(checkbox).toBeEnabled()
expect(checkbox).not.toBeChecked()
})
```

`getByRole` makes sure there is an accessible (for the user but also for the machine) checkbox element with an associated and readable name to it. It will fail if someone removes `type="checkbox"` by accident from the input or replaces the input with a `div`, which could also would affect functionality.

It’s not necessary to use `toBeVisible()` on e.g. form elements. If they are not visible, `getByRole` will throw an error - they are not accessible if they are not visible. (There’s a feature to actually test hidden elements by their role, but there’s almost never a need for it). Also, `toBeEnabled()` is implicitly confirming that an element is visible.

`.toBeChecked()` is another custom matcher from jest-dom. Only a valid, semantic and at least mostly accessible html element can be asserted with this. It can't be used on a `button type="button"` for example. This implicitly make sure all these things are working correctly.

#### How to structure test files 

The first `describe` block:

- wraps all the tests for one file
- contains only the name of the file we are testing (e.g. `MyComponent.tsx` or `fancy-util.ts`)

Example React component

```typescript
describe('Dashboard.tsx', () => {
  // all related 'describe's, and 'it's
})
```

Example ts-file

```typescript
describe('fancy-util.tsx', () => {
  // all related 'describe's, and 'it's
})
```

#### How to group tests meaningful

Group your tests with nested, meaningful describe scopes. 

Jest offers global methods to create blocks of tests. The most used for us are [describe](https://jestjs.io/docs/api#describename-fn) and [it](https://jestjs.io/docs/api#testname-fn-timeout) (it is an alias for test).

    `describe` creates a block that groups together several related tests.
    `it` is the method that actually runs one test.

Grouping several `it` in a `describe` block and nesting `describe` blocks create a more structured test file. It also translates to a nicely readable output in the test runner. 

A `describe` block describes a **scenario or test case**. An `it` block describes the specification of different assertions in this scenario. A rule of the thumb for unit tests is to **only have one assertion (expect) in one `it` block**. This helps us find the cause of a failing test faster. Grouping multiple `it` blocks in one `describe` makes it easier to archive this.

The nesting with `describe` blocks should ot be overused. 

**️✅ Do**

```jsx

describe('nameOfFunction', () => {  
  it('returns true when user chose "cat"', ...)

  it('returns true when user chose "dog"', ...)
})

```

**⛔️ Don't**

```tsx
describe('nameOfFunction', () => {
  describe('when user chose "cat', () => {
    it('returns true', ...)  })
  describe('when user chose "dog"', () => {
    it('return false', ...)
  })
})
```

##### Example: Grouping tests that check the default markup is rendered

```tsx
describe('MyNiceComponent.tsx', () => {
  describe('renders a page with default props', () => {
    it('shows a headline', () => {
     ...
    })
    it('shows an input field', () => {
     ...
    })
    it('shows a disabled button', () => {
     ...
    })
  })
  describe('now there are more tests', () => {
  })
})
```

This generates the following output when running the tests:

```markup
MyNiceComponent.tsx
  shows a page with default props
    ✓ renders a headline
    ✓ renders an input field
    ✓ renders a disabled button
```

##### Example: Grouping tests that test user interaction

```jsx
describe('MyNiceComponent.tsx', () => {
  //...
  describe('user can fill out the email field and send the form', () => {

    it('shows a disabled "Send" button while the input is empty', () => {
     //...
    })

    it('enables user to fill out input field', () => {
     //...
    })

    it('shows an enabled "Send" button when user filled out input', () => {
     //...
    })

    it('enables user to send the form with their input', () => {
     //...
    })
  })

  describe('now there are more tests', () => {
      //...
  })
})
```

The test output now looks like this:

```markup
MyNiceComponent.tsx
  shows a page with default props
    ✓ shows a headline
    ✓ shows an input field
    ✓ shows a disabled button
  user can fill out the email field and send the form
    ✓ shows a disabled "Send" button while the input is empty
    ✓ enables user to fill out input field
    ✓ shows an enabled "Send" button when user filled out input
    ✓ enables user to send the form with their input
```


### How to write test descriptions

We should use a consistent approach and language in our test description. This help to understand and document the code they are referring to!

🔖 You can read more in the article [Art of test case writing](http://softwaretestingtimes.com/2010/04/art-of-test-case-writing.html). 

#### General tip

Use the `describe` block to describe a **scenario or test case**.

This block can:
- describe a larger block of functionality.
- contain a set of specific requirements for one test case.
- group one user-path.

Examples:

- describe *“renders all necessary elements"*
- describe *“when user selects a certain environment"*
- describe *“handles the form for deleting a topic"*

The `it` block contains the specification itself. Together with its assertion it describes the expected outcome. Keep in mind that **one-assertion-per-it** is a good rule to follow for small tests. 

We read the `it` together with the description itself:

```jsx
it('closes the modal when user confirms')
```

Is read as *“it closes the modal when user confirms"*

Examples:

- it *“shows an input element"*
- it *“changes the color when user clicks button"*
- it *“disables the button when required input is missing"*

#### Recommended rules

##### 1. Use uncomplicated language

In general: Uncomplicated, user-centered language without unneeded implementation-related context makes test readable. It helps others to understand your code better. Keep in mind that not all people are native english speaker!

It’s shorter, clearer, more emphatic and to-the-point.

**️✅ Do**
- `it("filters a list based on user input")`

**⛔️ Don't**
- `it("a list is filtered based on string submitted by the user")`

##### 3. Use simple present tense for expected results

It’s a good way to express general truths or unchanging situations in an easy-to-read way.

**️✅ Do**
- `it("warns the user when they click delete")`

**⛔️ Don't**
- `it("will warn the user when they clicked delete")`

##### 4. Use direct and unambiguous language

Don’t use modal verbs! Make direct and unambiguous statements about the expected outcome. The test is not to make sure code *should* (hopefully) do something. The test makes sure the code *does* this.

**️✅ Do**
- `it("shows list in the right order")`
- 
**⛔️ Don't**
- `it("should show list in the right order")`


##### 5. Use user-based language for React component tests

For React component tests, try to focus on a more user-based language. Don’t add unnecessary implementation details to the descriptions.

**️✅ Do**
- `it("shows a list of topics filtered by team")`

**⛔️ Don't**
- `it("should call the getTopicByTeam api to fetch new list items")`