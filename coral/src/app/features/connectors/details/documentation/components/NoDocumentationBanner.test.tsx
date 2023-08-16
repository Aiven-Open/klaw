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

    it("shows headline No readme", () => {
      const headline = screen.getByRole("heading", {
        name: "No readme available",
      });
      expect(headline).toBeVisible();
    });

    it("shows information that no readme is available for this connector", () => {
      const infoText = screen.getByText(
        "Add a readme to give your team essential information, guidelines, and context about the connector."
      );
      expect(infoText).toBeVisible();
    });

    it("shows button to add a readme", () => {
      const button = screen.getByRole("button", { name: "Add readme" });
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

    it("adds readme when user clicks button", async () => {
      const button = screen.getByRole("button", { name: "Add readme" });
      await user.click(button);

      expect(testAddDocumentation).toHaveBeenCalled();
    });
  });
});
