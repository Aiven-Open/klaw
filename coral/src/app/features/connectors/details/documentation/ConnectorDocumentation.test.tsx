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
  } as ConnectorOverview["connectorInfo"],
  connectorExists: true,
};

const mockConnectorDetails = {
  connectorIsRefetching: false,
  environmentId: "1",
  connectorOverview: testConnectorOverview,
};

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

describe("ConnectorDocumentation", () => {
  beforeAll(mockIntersectionObserver);

  const user = userEvent.setup();

  describe("if the connector has no documentation yet", () => {
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

      it("shows headline No Documentation", () => {
        const headline = screen.getByRole("heading", {
          name: "No documentation",
        });
        expect(headline).toBeVisible();
      });

      it("shows a button to add documentation", () => {
        const addDocumentationButton = screen.getByRole("button", {
          name: "Add documentation",
        });
        expect(addDocumentationButton).toBeEnabled();
      });
    });

    describe("enables user to add documentation", () => {
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

      it("shows the edit mode when user clicks button to add documentation", async () => {
        const addDocumentationButton = screen.getByRole("button", {
          name: "Add documentation",
        });
        const headlineNoDocumentation = screen.getByRole("heading", {
          name: "No documentation",
        });

        expect(headlineNoDocumentation).toBeVisible();
        await user.click(addDocumentationButton);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        const headlineEdit = screen.getByRole("heading", {
          name: "Edit documentation",
        });

        expect(markdownEditor).toBeVisible();
        expect(headlineNoDocumentation).not.toBeInTheDocument();
        expect(headlineEdit).toBeVisible();
      });
    });
  });

  describe("if the connector has existing documentation", () => {
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

      it("shows documentation headline", () => {
        const headline = screen.getByRole("heading", { name: "Documentation" });

        expect(headline).toBeVisible();
      });

      it("shows the documentation", () => {
        const markdownView = screen.getByTestId("react-markdown-mock");

        expect(markdownView).toBeVisible();
        expect(markdownView).toHaveTextContent(existingDocumentation);
      });

      it("shows a button to edit documentation", () => {
        const addDocumentationButton = screen.getByRole("button", {
          name: "Edit documentation",
        });
        expect(addDocumentationButton).toBeEnabled();
      });
    });

    describe("enables user to edit documentation", () => {
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

      it("shows the edit mode when user clicks button to edit documentation", async () => {
        const editDocumentation = screen.getByRole("button", {
          name: "Edit documentation",
        });

        await user.click(editDocumentation);

        const markdownEditor = screen.getByRole("textbox", {
          name: "Markdown editor",
        });
        const headlineEdit = screen.getByRole("heading", {
          name: "Edit documentation",
        });

        expect(markdownEditor).toBeVisible();
        expect(headlineEdit).toBeVisible();
      });
    });
  });

  describe("if documentation is updating", () => {
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

    it("shows documentation headline", () => {
      const headline = screen.getByRole("heading", { name: "Documentation" });

      expect(headline).toBeVisible();
    });

    it("shows accessible information about loading documentation", () => {
      const loadingInformation = screen.getByText("Loading documentation");

      expect(loadingInformation).toBeVisible();
      expect(loadingInformation).toHaveClass("visually-hidden");
    });

    it("shows no documentation", () => {
      const markdownView = screen.queryByTestId("react-markdown-mock");

      expect(markdownView).not.toBeInTheDocument();
    });

    it("shows no button to edit documentation", () => {
      const addDocumentationButton = screen.queryByRole("button", {
        name: "Edit documentation",
      });
      expect(addDocumentationButton).not.toBeInTheDocument();
    });
  });

  describe("enables user to update documentation", () => {
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

    it("enables user to cancel editing the documentation", async () => {
      const editButton = screen.getByRole("button", {
        name: "Edit documentation",
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

    it("saves documentation when user clicks button", async () => {
      const editButton = screen.getByRole("button", {
        name: "Edit documentation",
      });

      await user.click(editButton);

      const markdownEditor = screen.getByRole("textbox", {
        name: "Markdown editor",
      });
      await user.type(markdownEditor, userInput);

      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.click(saveButton);

      expect(mockUpdateConnectorDocumentation).toHaveBeenCalledWith({
        connectorName: "documentation-test-connector",
        connectorIdForDocumentation: 99999,
        connectorDocumentation: existingDocumentation + userInput,
      });
    });
    it("shows preview mode after successful update", async () => {
      const editDocumentation = screen.getByRole("button", {
        name: "Edit documentation",
      });

      await user.click(editDocumentation);

      const markdownEditor = screen.getByRole("textbox", {
        name: "Markdown editor",
      });
      await user.type(markdownEditor, userInput);

      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.click(saveButton);

      const markdownView = await screen.findByTestId("react-markdown-mock");
      expect(markdownEditor).not.toBeVisible();
      expect(markdownView).toBeVisible();
    });
  });

  describe("handles errors when transforming documentation intro correct markdown from backend", () => {
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
        "Something went wrong while trying to transform the documentation into the right format."
      );

      const previewMode = screen.queryByTestId("react-markdown-mock");
      const button = screen.queryByRole("button");

      expect(previewMode).not.toBeInTheDocument();
      expect(button).not.toBeInTheDocument();
    });
  });

  describe("handles errors with updating documentation", () => {
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

    it("shows errors without saving documentation when user clicks button", async () => {
      const editButton = screen.getByRole("button", {
        name: "Edit documentation",
      });

      await user.click(editButton);

      const markdownEditor = screen.getByRole("textbox", {
        name: "Markdown editor",
      });
      await user.type(markdownEditor, userInput);

      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.click(saveButton);

      const error = screen.getByRole("alert");
      expect(error).toBeVisible();
      expect(error).toHaveTextContent(
        "The documentation could not be saved, there was an error"
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
