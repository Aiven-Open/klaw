import { NoDocumentationBanner } from "src/app/features/connectors/details/documentation/components/NoDocumentationBanner";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const testAddDocumentation = jest.fn();

describe("NoDocumentationBanner", () => {
  const user = userEvent.setup();

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<NoDocumentationBanner addDocumentation={testAddDocumentation} />);
    });

    afterAll(cleanup);

    it("shows headline No Documentation", () => {
      const headline = screen.getByRole("heading", {
        name: "No documentation",
      });
      expect(headline).toBeVisible();
    });

    it("shows information that no documentation is available for this connector", () => {
      const infoText = screen.getByText(
        "You can add documentation for your connector."
      );
      expect(infoText).toBeVisible();
    });

    it("shows button to add a documentation", () => {
      const button = screen.getByRole("button", { name: "Add documentation" });
      expect(button).toBeVisible();
    });
  });

  describe("triggers a addDocumentation event", () => {
    beforeEach(() => {
      render(<NoDocumentationBanner addDocumentation={testAddDocumentation} />);
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("adds documentation when user clicks button", async () => {
      const button = screen.getByRole("button", { name: "Add documentation" });
      await user.click(button);

      expect(testAddDocumentation).toHaveBeenCalled();
    });
  });
});
