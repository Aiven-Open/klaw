import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { cleanup, render, screen } from "@testing-library/react";

describe("NoDocumentationBanner", () => {
  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<NoDocumentationBanner />);
    });

    afterAll(cleanup);

    it("shows headline No Documentation", () => {
      const headline = screen.getByRole("heading", {
        name: "No documentation",
      });
      expect(headline).toBeVisible();
    });

    it("shows information that no documentation is available for this topic", () => {
      const infoText = screen.getByText(
        "You can add documentation for your topic."
      );
      expect(infoText).toBeVisible();
    });

    it("shows button to add a documentation", () => {
      const button = screen.getByRole("button", { name: "Add documentation" });
      expect(button).toBeVisible();
    });
  });
});
