import { render, screen } from "@testing-library/react/pure";
import NotFound from "src/app/pages/not-found";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

describe("NotFound", () => {
  describe("renders a Not Found page with correct text", () => {
    beforeAll(() => {
      render(<NotFound />);
    });

    it("renders a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Page not found",
      });

      expect(headline).toBeVisible();
    });

    it("renders a description", async () => {
      const description = screen.getByText(
        "If it should have been found, we are working on building it!"
      );

      expect(description).toBeVisible();
    });

    it("renders a link to old interface index page", async () => {
      const link = screen.getByRole("link", {
        name: "Go back to old interface",
      });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute("href", "/index");
    });

    it("should allow navigating to link with keyboard", async () => {
      const link = screen.getByRole("link", {
        name: "Go back to old interface",
      });

      await tabNavigateTo({ targetElement: link });

      expect(link).toHaveFocus();
    });
  });
});
