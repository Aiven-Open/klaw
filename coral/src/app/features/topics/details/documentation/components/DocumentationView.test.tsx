import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";
import { cleanup, render, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";

const testStringifiedHtml = `<h1>Hello</h1>World!<b>Great</b>to see you!`;

jest.mock("rehype-raw");
describe("DocumentationView", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<DocumentationView stringifiedHtml={testStringifiedHtml} />);
    });

    afterAll(cleanup);

    it("renders an article to separate docs semantically", () => {
      const article = screen.getByRole("article");
      expect(article).toBeVisible();
    });

    it("shows the documentation based on stringified HTML", () => {
      const reactMarkdownMock = screen.getByTestId("react-markdown-mock");
      expect(reactMarkdownMock).toBeVisible();
    });

    it("passes the stringified HTML to the markdown element", () => {
      const reactMarkdownMock = screen.getByTestId("react-markdown-mock");

      // due to the react-markdown mock the html is escaped, the test
      // should only verify it's passed at children
      const escapedHtml = String(testStringifiedHtml);
      const text = within(reactMarkdownMock).getByText(escapedHtml);

      expect(text).toBeVisible();
    });
  });
});
