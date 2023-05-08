import { screen } from "@testing-library/react/pure";
import NotFound from "src/app/pages/not-found";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

describe("NotFound", () => {
  describe("renders a Not Found page with correct text", () => {
    beforeAll(() => {
      customRender(<NotFound />, { memoryRouter: true });
    });

    it("renders a headline", () => {
      const headline = screen.getByRole("heading", {
        name: "Page not found",
      });

      expect(headline).toBeVisible();
    });

    it("renders a description", () => {
      const description = screen.getByText(
        "Sorry, the page you are looking for does not exist."
      );

      expect(description).toBeVisible();
    });

    it("renders a link to old interface index page", () => {
      const link = screen.getByRole("link", {
        name: "Return to the old interface.",
      });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute("href", "/index");
    });

    it("should allow navigating to link with keyboard", async () => {
      const link = screen.getByRole("link", {
        name: "Return to the old interface.",
      });

      await tabNavigateTo({ targetElement: link });

      expect(link).toHaveFocus();
    });
  });
});
