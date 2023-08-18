import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorDocumentation } from "src/app/features/connectors/details/documentation/ConnectorDocumentation";
import {
  ConnectorDocumentationMarkdown,
  ConnectorOverview,
  updateConnectorDocumentation,
} from "src/domain/connector";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/app/features/connectors/details/ConnectorDetails");
const mockUseConnectorDetails = useConnectorDetails as jest.MockedFunction<
  typeof useConnectorDetails
>;

jest.mock("src/domain/connector/connector-api.ts");
const mockUpdateConnectorDocumentation =
  updateConnectorDocumentation as jest.MockedFunction<
    typeof updateConnectorDocumentation
  >;

const mockIsDocumentationTransformationError = jest.fn();
jest.mock("src/domain/helper/documentation-helper", () => ({
  isDocumentationTransformationError: () =>
    mockIsDocumentationTransformationError(),
}));

const testConnectorOverview: ConnectorOverview = {
  availableEnvironments: [],
  connectorIdForDocumentation: 99999,
  connectorInfo: {
    connectorName: "documentation-test-connector",
    connectorOwner: true,
  } as ConnectorOverview["connectorInfo"],
  connectorExists: true,
};

const mockConnectorDetails = {
  connectorIsRefetching: false,
  environmentId: "1",
  connectorOverview: testConnectorOverview,
};

const mockConnectorDetailsIsNotConnectorOwner = {
  connectorIsRefetching: false,
  environmentId: "1",
  connectorOverview: {
    ...testConnectorOverview,
    connectorInfo: {
      connectorName: "documentation-test-connector",
      connectorOwner: false,
    } as ConnectorOverview["connectorInfo"],
  },
};

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

describe("ConnectorDocumentation", () => {
  beforeAll(mockIntersectionObserver);

  const user = userEvent.setup();

  describe("handles documentation for connector owner", () => {
    describe("if the connector has no readme yet", () => {
      describe("shows all necessary elements", () => {
        beforeAll(() => {
          mockUseConnectorDetails.mockReturnValue(mockConnectorDetails);
          mockUpdateConnectorDocumentation.mockResolvedValue({
            success: true,
            message: "",
          });
          customRender(
            <AquariumContext>
              <ConnectorDocumentation />
            </AquariumContext>,
            { queryClient: true }
          );
        });

        afterAll(cleanup);

        it("shows headline No readme available", () => {
          const headline = screen.getByRole("heading", {
            name: "No readme available",
          });
          expect(headline).toBeVisible();
        });

        it("shows a button to add readme", () => {
          const addReadmeButton = screen.getByRole("button", {
            name: "Add readme",
          });
          expect(addReadmeButton).toBeEnabled();
        });
      });

      describe("enables user to add readme", () => {
        beforeEach(() => {
          mockUseConnectorDetails.mockReturnValue(mockConnectorDetails);
          mockUpdateConnectorDocumentation.mockResolvedValue({
            success: true,
            message: "",
          });
          customRender(
            <AquariumContext>
              <ConnectorDocumentation />
            </AquariumContext>,
            { queryClient: true }
          );
        });

        afterEach(cleanup);

        it("shows no editor on default", () => {
          const markdownEditor = screen.queryByRole("textbox", {
            name: "Markdown editor",
          });
          expect(markdownEditor).not.toBeInTheDocument();
        });

        it("shows the edit mode when user clicks button to add readme", async () => {
          const addReadmeButton = screen.getByRole("button", {
            name: "Add readme",
          });
          const headlineNoReadme = screen.getByRole("heading", {
            name: "No readme available",
          });

          expect(headlineNoReadme).toBeVisible();
          await user.click(addReadmeButton);

          const markdownEditor = screen.getByRole("textbox", {
            name: "Markdown editor",
          });
          const headlineEdit = screen.getByRole("heading", {
            name: "Edit readme",
          });

          expect(markdownEditor).toBeVisible();
          expect(headlineNoReadme).not.toBeInTheDocument();
          expect(headlineEdit).toBeVisible();
        });

        it("shows a description about the readme", async () => {
          const addReadmeButton = screen.getByRole("button", {
            name: "Add readme",
          });
          await user.click(addReadmeButton);

          const description = screen.getByText(
            `Readme provides essential information, guidelines, and explanations about the connector, helping team members understand its purpose and usage. Edit the readme to update the information as the connector evolves.`
          );

          expect(description).toBeVisible();
        });
      });
    });

    describe("if the connector has existing readme", () => {
      const existingDocumentation = "# Hello" as ConnectorDocumentationMarkdown;

      describe("shows all necessary elements", () => {
        beforeAll(() => {
          mockUseConnectorDetails.mockReturnValue({
            ...mockConnectorDetails,
            connectorOverview: {
              ...mockConnectorDetails.connectorOverview,
              connectorDocumentation: existingDocumentation,
            },
          });

          mockUpdateConnectorDocumentation.mockResolvedValue({
            success: true,
            message: "",
          });

          customRender(
            <AquariumContext>
              <ConnectorDocumentation />
            </AquariumContext>,
            { queryClient: true }
          );
        });

        afterAll(cleanup);

        it("shows readme headline", () => {
          const headline = screen.getByRole("heading", { name: "Readme" });

          expect(headline).toBeVisible();
        });

        it("shows a description about the readme", () => {
          const description = screen.getByText(
            `Readme provides essential information, guidelines, and explanations about the connector, helping team members understand its purpose and usage. Edit the readme to update the information as the connector evolves.`
          );

          expect(description).toBeVisible();
        });

        it("shows the readme", () => {
          const markdownView = screen.getByTestId("react-markdown-mock");

          expect(markdownView).toBeVisible();
          expect(markdownView).toHaveTextContent(existingDocumentation);
        });

        it("shows a button to edit readme", () => {
          const addReadmeButton = screen.getByRole("button", {
            name: "Edit readme",
          });
          expect(addReadmeButton).toBeEnabled();
        });
      });

      describe("enables user to edit readme", () => {
        beforeEach(() => {
          mockUseConnectorDetails.mockReturnValue({
            ...mockConnectorDetails,
            connectorOverview: {
              ...mockConnectorDetails.connectorOverview,
              connectorDocumentation: existingDocumentation,
            },
          });

          customRender(
            <AquariumContext>
              <ConnectorDocumentation />
            </AquariumContext>,
            { queryClient: true }
          );
        });

        afterEach(cleanup);

        it("shows the edit mode when user clicks button to edit readme", async () => {
          const editReadme = screen.getByRole("button", {
            name: "Edit readme",
          });

          await user.click(editReadme);

          const markdownEditor = screen.getByRole("textbox", {
            name: "Markdown editor",
          });
          const headlineEdit = screen.getByRole("heading", {
            name: "Edit readme",
          });

          expect(markdownEditor).toBeVisible();
          expect(headlineEdit).toBeVisible();
        });
      });
    });

    describe("if readme is updating", () => {
      const existingDocumentation = "# Hello" as ConnectorDocumentationMarkdown;

      beforeAll(() => {
        mockUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorIsRefetching: true,
          connectorOverview: {
            ...mockConnectorDetails.connectorOverview,
            connectorDocumentation: existingDocumentation,
          },
        });

        mockUpdateConnectorDocumentation.mockResolvedValue({
          success: true,
          message: "",
        });

        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterAll(cleanup);

      it("shows readme headline", () => {
        const headline = screen.getByRole("heading", { name: "Readme" });

        expect(headline).toBeVisible();
      });

      it("shows accessible information about loading readme", () => {
        const loadingInformation = screen.getByText("Loading readme");

        expect(loadingInformation).toBeVisible();
        expect(loadingInformation).toHaveClass("visually-hidden");
      });

      it("shows no readme", () => {
        const markdownView = screen.queryByTestId("react-markdown-mock");

        expect(markdownView).not.toBeInTheDocument();
      });

      it("shows no button to edit readme", () => {
        const addReadmeButton = screen.queryByRole("button", {
          name: "Edit readme",
        });
        expect(addReadmeButton).not.toBeInTheDocument();
      });
    });

    describe("enables user to update readme", () => {
      const existingDocumentation = "# Hello" as ConnectorDocumentationMarkdown;
      const userInput = "**Hello world**";

      beforeEach(() => {
        mockUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...mockConnectorDetails.connectorOverview,
            connectorDocumentation: existingDocumentation,
          },
        });
        mockUpdateConnectorDocumentation.mockResolvedValue({
          success: true,
          message: "",
        });
        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it("enables user to cancel editing the readme", async () => {
        const editButton = screen.getByRole("button", {
          name: "Edit readme",
        });

        await user.click(editButton);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        await user.type(markdownEditor, userInput);

        const cancelButton = screen.getByRole("button", {
          name: "Cancel",
        });

        await user.click(cancelButton);

        expect(mockUpdateConnectorDocumentation).not.toHaveBeenCalled();
        expect(markdownEditor).not.toBeInTheDocument();

        const previewMode = screen.getByTestId("react-markdown-mock");
        expect(previewMode).toBeVisible();
      });

      it("saves readme when user clicks button", async () => {
        const editButton = screen.getByRole("button", {
          name: "Edit readme",
        });

        await user.click(editButton);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        await user.type(markdownEditor, userInput);

        const saveButton = screen.getByRole("button", {
          name: "Save readme",
        });

        await user.click(saveButton);

        expect(mockUpdateConnectorDocumentation).toHaveBeenCalledWith({
          connectorName: "documentation-test-connector",
          connectorIdForDocumentation: 99999,
          connectorDocumentation: existingDocumentation + userInput,
        });
      });

      it("shows preview mode after successful update", async () => {
        const editReadme = screen.getByRole("button", {
          name: "Edit readme",
        });

        await user.click(editReadme);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        await user.type(markdownEditor, userInput);

        const saveButton = screen.getByRole("button", {
          name: "Save readme",
        });

        await user.click(saveButton);

        const markdownView = await screen.findByTestId("react-markdown-mock");
        expect(markdownEditor).not.toBeVisible();
        expect(markdownView).toBeVisible();
      });
    });

    describe("handles errors when transforming readme intro correct markdown from backend", () => {
      const originalConsoleError = console.error;
      beforeEach(() => {
        console.error = jest.fn();
        mockIsDocumentationTransformationError.mockReturnValue(true);
        mockUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...mockConnectorDetails.connectorOverview,
            connectorDocumentation:
              "an error will happen" as ConnectorDocumentationMarkdown,
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterEach(() => {
        console.error = originalConsoleError;
        jest.resetAllMocks();
        cleanup();
      });

      it("shows error informing user that something went wrong instead of preview and buttons", async () => {
        const errorInformation = screen.getByRole("alert");

        expect(errorInformation).toBeVisible();
        expect(errorInformation).toHaveTextContent(
          "Something went wrong while trying to transform the readme into the right format."
        );

        const previewMode = screen.queryByTestId("react-markdown-mock");
        const button = screen.queryByRole("button");

        expect(previewMode).not.toBeInTheDocument();
        expect(button).not.toBeInTheDocument();
      });
    });

    describe("handles errors with updating readme", () => {
      const existingDocumentation = "# Hello" as ConnectorDocumentationMarkdown;
      const userInput = "**Hello world**";

      const originalConsoleError = console.error;
      beforeEach(() => {
        console.error = jest.fn();
        mockUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...mockConnectorDetails.connectorOverview,
            connectorDocumentation: existingDocumentation,
          },
        });
        mockUpdateConnectorDocumentation.mockRejectedValue({
          success: false,
          message: "this is error",
        });

        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterEach(() => {
        console.error = originalConsoleError;
        jest.resetAllMocks();
        cleanup();
      });

      it("shows errors without saving readme when user clicks button", async () => {
        const editButton = screen.getByRole("button", {
          name: "Edit readme",
        });

        await user.click(editButton);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        await user.type(markdownEditor, userInput);

        const saveButton = screen.getByRole("button", {
          name: "Save readme",
        });

        await user.click(saveButton);

        const error = screen.getByRole("alert");
        expect(error).toBeVisible();
        expect(error).toHaveTextContent(
          "The readme could not be saved, there was an error"
        );

        const previewMode = screen.queryByTestId("react-markdown-mock");
        expect(markdownEditor).toBeVisible();
        expect(previewMode).not.toBeInTheDocument();
        expect(console.error).toHaveBeenCalledWith({
          success: false,
          message: "this is error",
        });
      });
    });
  });

  describe("handles documentation user that are not connector owner", () => {
    describe("if the connector has no documentation yet", () => {
      beforeAll(() => {
        mockUseConnectorDetails.mockReturnValue(
          mockConnectorDetailsIsNotConnectorOwner
        );
        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterAll(cleanup);

      it("shows headline No readme available", () => {
        const headline = screen.getByRole("heading", {
          name: "No readme available",
        });
        expect(headline).toBeVisible();
      });

      it("shows no button to add readme", () => {
        const addReadmeButton = screen.queryByRole("button");

        expect(addReadmeButton).not.toBeInTheDocument();
      });
    });

    describe("if the connector has existing readme", () => {
      const existingReadme = "# Hello" as ConnectorDocumentationMarkdown;

      beforeAll(() => {
        mockUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetailsIsNotConnectorOwner,
          connectorOverview: {
            ...mockConnectorDetailsIsNotConnectorOwner.connectorOverview,
            connectorDocumentation: existingReadme,
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorDocumentation />
          </AquariumContext>,
          { queryClient: true }
        );
      });

      afterAll(cleanup);

      it("shows readme headline", () => {
        const headline = screen.getByRole("heading", { name: "Readme" });

        expect(headline).toBeVisible();
      });

      it("shows a description about the readme", () => {
        const description = screen.getByText(
          `Readme provides essential information, guidelines, and explanations about the connector, helping team members understand its purpose and usage.`
        );

        expect(description).toBeVisible();
      });

      it("does not show editing information in the description for the readme", () => {
        const description = screen.queryByText(
          `Edit the readme to update the information as the connector evolves.`
        );

        expect(description).not.toBeInTheDocument();
      });

      it("shows the readme", () => {
        const markdownView = screen.getByTestId("react-markdown-mock");

        expect(markdownView).toBeVisible();
        expect(markdownView).toHaveTextContent(existingReadme);
      });

      it("shows no button to edit readme", () => {
        const addReadmeButton = screen.queryByRole("button");

        expect(addReadmeButton).not.toBeInTheDocument();
      });
    });
  });
});
