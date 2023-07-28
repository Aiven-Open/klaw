import { cleanup, screen } from "@testing-library/react";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

describe("InternalLinkButton", () => {
  mockIntersectionObserver();
  const testTo = "/test/url";
  const testText = "My nice link";

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      customRender(
        <InternalLinkButton to={testTo}>{testText}</InternalLinkButton>,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows a link", () => {
      const link = screen.getByRole("link", { name: testText });

      expect(link).toBeVisible();
    });

    it("renders a given route as 'href'", () => {
      const link = screen.getByRole("link", { name: testText });

      expect(link).toHaveAttribute("href", testTo);
    });
  });

  describe("renders different styles based on selected Aquarium's Button props", () => {
    afterEach(cleanup);

    it("shows a primary button by default", () => {
      const { container } = customRender(
        <InternalLinkButton to={testTo}>{testText}</InternalLinkButton>,
        { browserRouter: true }
      );

      expect(container).toMatchSnapshot();
    });

    it("shows a secondary button based on props", () => {
      const { container } = customRender(
        <InternalLinkButton to={testTo} kind={"secondary"}>
          {testText}
        </InternalLinkButton>,
        { browserRouter: true }
      );

      expect(container).toMatchSnapshot();
    });
  });
});
