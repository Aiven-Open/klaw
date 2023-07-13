import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicDocumentation } from "src/app/features/topics/details/documentation/TopicDocumentation";
import {
  TopicDocumentationMarkdown,
  TopicOverview,
  updateTopicDocumentation,
} from "src/domain/topic";
import { TopicSchemaOverview } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/app/features/topics/details/TopicDetails");
const mockUseTopicDetails = useTopicDetails as jest.MockedFunction<
  typeof useTopicDetails
>;

jest.mock("src/domain/topic/topic-api.ts");
const mockUpdateTopicDocumentation =
  updateTopicDocumentation as jest.MockedFunction<
    typeof updateTopicDocumentation
  >;

const mockIsDocumentationTransformationError = jest.fn();
jest.mock("src/domain/helper/documentation-helper", () => ({
  isDocumentationTransformationError: () =>
    mockIsDocumentationTransformationError(),
}));

const testTopicOverview: TopicOverview = {
  availableEnvironments: [],
  prefixAclsExists: false,
  schemaExists: false,
  txnAclsExists: false,
  topicPromotionDetails: { status: "NO_PROMOTION" },
  topicIdForDocumentation: 99999,
  topicInfo: {
    topicName: "documentation-test-topic",
  } as TopicOverview["topicInfo"],
  topicExists: true,
};

const mockTopicDetails = {
  topicOverviewIsRefetching: false,
  topicSchemasIsRefetching: false,
  environmentId: "1",
  topicName: "hello",
  topicOverview: testTopicOverview,
  topicSchemas: {} as TopicSchemaOverview,
  setSchemaVersion: jest.fn(),
};

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

describe("TopicDocumentation", () => {
  beforeAll(mockIntersectionObserver);

  const user = userEvent.setup();

  describe("if the topic has no documentation yet", () => {
    describe("shows all necessary elements", () => {
      beforeAll(() => {
        mockUseTopicDetails.mockReturnValue(mockTopicDetails);
        mockUpdateTopicDocumentation.mockResolvedValue({
          success: true,
          message: "",
        });
        customRender(
          <AquariumContext>
            <TopicDocumentation />
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
        mockUseTopicDetails.mockReturnValue(mockTopicDetails);
        mockUpdateTopicDocumentation.mockResolvedValue({
          success: true,
          message: "",
        });
        customRender(
          <AquariumContext>
            <TopicDocumentation />
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

  describe("if the topic has existing documentation", () => {
    const existingDocumentation = "# Hello" as TopicDocumentationMarkdown;

    describe("shows all necessary elements", () => {
      beforeAll(() => {
        mockUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...mockTopicDetails.topicOverview,
            topicDocumentation: existingDocumentation,
          },
        });

        mockUpdateTopicDocumentation.mockResolvedValue({
          success: true,
          message: "",
        });

        customRender(
          <AquariumContext>
            <TopicDocumentation />
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
        mockUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...mockTopicDetails.topicOverview,
            topicDocumentation: existingDocumentation,
          },
        });

        customRender(
          <AquariumContext>
            <TopicDocumentation />
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
    const existingDocumentation = "# Hello" as TopicDocumentationMarkdown;

    beforeAll(() => {
      mockUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverviewIsRefetching: true,
        topicOverview: {
          ...mockTopicDetails.topicOverview,
          topicDocumentation: existingDocumentation,
        },
      });

      mockUpdateTopicDocumentation.mockResolvedValue({
        success: true,
        message: "",
      });

      customRender(
        <AquariumContext>
          <TopicDocumentation />
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
    const existingDocumentation = "# Hello" as TopicDocumentationMarkdown;
    const userInput = "**Hello world**";

    beforeEach(() => {
      mockUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverview: {
          ...mockTopicDetails.topicOverview,
          topicDocumentation: existingDocumentation,
        },
      });
      mockUpdateTopicDocumentation.mockResolvedValue({
        success: true,
        message: "",
      });
      customRender(
        <AquariumContext>
          <TopicDocumentation />
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

      expect(mockUpdateTopicDocumentation).not.toHaveBeenCalled();
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

      expect(mockUpdateTopicDocumentation).toHaveBeenCalledWith({
        topicName: "documentation-test-topic",
        topicIdForDocumentation: 99999,
        topicDocumentation: existingDocumentation + userInput,
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
      mockUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverview: {
          ...mockTopicDetails.topicOverview,
          topicDocumentation:
            "an error will happen" as TopicDocumentationMarkdown,
        },
      });

      customRender(
        <AquariumContext>
          <TopicDocumentation />
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
    const existingDocumentation = "# Hello" as TopicDocumentationMarkdown;
    const userInput = "**Hello world**";

    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = jest.fn();
      mockUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverview: {
          ...mockTopicDetails.topicOverview,
          topicDocumentation: existingDocumentation,
        },
      });
      mockUpdateTopicDocumentation.mockRejectedValue({
        success: false,
        message: "this is error",
      });

      customRender(
        <AquariumContext>
          <TopicDocumentation />
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
