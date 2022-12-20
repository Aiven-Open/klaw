import { render, screen } from "@testing-library/react/pure";
import NotFound from "src/app/pages/not-found";

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
  });
});
