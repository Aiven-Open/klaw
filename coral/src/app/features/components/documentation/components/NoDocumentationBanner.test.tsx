import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { NoDocumentationBanner } from "src/app/features/components/documentation/components/NoDocumentationBanner";

const testAddDocumentation = jest.fn();

describe("NoDocumentationBanner", () => {
  const user = userEvent.setup();

  describe("if user is entity owner", () => {
    describe("shows all necessary elements for entity topic", () => {
      beforeAll(() => {
        render(
          <NoDocumentationBanner
            entity={"topic"}
            addDocumentation={testAddDocumentation}
            isUserOwner={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows headline No Documentation", () => {
        const headline = screen.getByRole("heading", {
          name: "No readme available",
        });
        expect(headline).toBeVisible();
      });

      it("shows information that no readme is available for this topic", () => {
        const infoText = screen.getByText(
          "Add a readme to give your team essential information, guidelines, and context about the topic."
        );
        expect(infoText).toBeVisible();
      });

      it("shows button to add a readme", () => {
        const button = screen.getByRole("button", { name: "Add readme" });
        expect(button).toBeVisible();
      });
    });

    describe("shows all necessary elements for entity connector", () => {
      beforeAll(() => {
        render(
          <NoDocumentationBanner
            entity={"connector"}
            addDocumentation={testAddDocumentation}
            isUserOwner={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows headline No Documentation", () => {
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
        render(
          <NoDocumentationBanner
            addDocumentation={testAddDocumentation}
            entity={"topic"}
            isUserOwner={true}
          />
        );
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

  describe("if user is not entity owner", () => {
    describe("shows all necessary elements", () => {
      beforeAll(() => {
        render(
          <NoDocumentationBanner
            entity={"topic"}
            addDocumentation={testAddDocumentation}
            isUserOwner={false}
          />
        );
      });

      afterAll(cleanup);

      it("shows headline No Documentation", () => {
        const headline = screen.getByRole("heading", {
          name: "No readme available",
        });
        expect(headline).toBeVisible();
      });

      it("shows no information that no readme is available for this topic", () => {
        const infoText = screen.queryByText(
          /Add a readme to give your team essential information/
        );
        expect(infoText).not.toBeInTheDocument();
      });

      it("shows  no button to add a readme", () => {
        const button = screen.queryByRole("button");
        expect(button).not.toBeInTheDocument();
      });
    });
  });
});
