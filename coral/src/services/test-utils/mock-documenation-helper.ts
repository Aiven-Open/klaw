// createMarkdown and createStringifiedHtml are using unified, which
// provides ES modules that jest can't handle (still). We're mocking
// our helper instead of trying to mock a process etc. to create
// a mock module in __mocks__
// this could be imported in the test file that execute codes that
// is using this functions, but it has to be imported in the right
// order to actually mock the module (before the import that uses
// the function that uses the mocked functions) and this is to brittle
// -> could change through a (automatic) reorder of imports etc.
// so we're importing this in setupAfterEnv to make sure it's mocked
// at the right time
jest.mock("src/domain/helper/documentation-helper", () => ({
  createMarkdown: (string: string | undefined) =>
    `create-markdown-mock-${string}`,
  createStringifiedHtml: (string: string | undefined) =>
    `create-stringified-html-${string}`,
}));
