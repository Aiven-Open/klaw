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
  createSchemaAllowed: false,
  topicPromotionDetails: { status: "NO_PROMOTION" },
  topicIdForDocumentation: 99999,
  topicInfo: {
    topicName: "documentation-test-topic",
    topicOwner: true,
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

const mockTopicDetailsNotTopicOwner = {
  topicOverviewIsRefetching: false,
  topicSchemasIsRefetching: false,
  environmentId: "1",
  topicName: "hello",
  topicOverview: {
    ...testTopicOverview,
    topicInfo: { ...testTopicOverview.topicInfo, topicOwner: false },
  },
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

  describe("handles documentation for topic owner", () => {
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
            `Readme provides essential information, guidelines, and explanations about the topic, helping team members understand its purpose and usage. Edit the readme to update the information as the topic evolves.`
          );

          expect(description).toBeVisible();
        });
      });
    });

    describe("if the topic has existing readme", () => {
      const existingReadme = "# Hello" as TopicDocumentationMarkdown;

      describe("shows all necessary elements", () => {
        beforeAll(() => {
          mockUseTopicDetails.mockReturnValue({
            ...mockTopicDetails,
            topicOverview: {
              ...mockTopicDetails.topicOverview,
              topicDocumentation: existingReadme,
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

        it("shows readme headline", () => {
          const headline = screen.getByRole("heading", { name: "Readme" });

          expect(headline).toBeVisible();
        });

        it("shows a description about the readme", () => {
          const description = screen.getByText(
            `Readme provides essential information, guidelines, and explanations about the topic, helping team members understand its purpose and usage. Edit the readme to update the information as the topic evolves.`
          );

          expect(description).toBeVisible();
        });

        it("shows the readme", () => {
          const markdownView = screen.getByTestId("react-markdown-mock");

          expect(markdownView).toBeVisible();
          expect(markdownView).toHaveTextContent(existingReadme);
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
          mockUseTopicDetails.mockReturnValue({
            ...mockTopicDetails,
            topicOverview: {
              ...mockTopicDetails.topicOverview,
              topicDocumentation: existingReadme,
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
      const existingReadme = "# Hello" as TopicDocumentationMarkdown;

      beforeAll(() => {
        mockUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverviewIsRefetching: true,
          topicOverview: {
            ...mockTopicDetails.topicOverview,
            topicDocumentation: existingReadme,
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
      const existingReadme = "# Hello" as TopicDocumentationMarkdown;
      const userInput = "**Hello world**";

      beforeEach(() => {
        mockUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...mockTopicDetails.topicOverview,
            topicDocumentation: existingReadme,
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

        expect(mockUpdateTopicDocumentation).not.toHaveBeenCalled();
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

        expect(mockUpdateTopicDocumentation).toHaveBeenCalledWith({
          topicName: "documentation-test-topic",
          topicIdForDocumentation: 99999,
          topicDocumentation: existingReadme + userInput,
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
          "Something went wrong while trying to transform the readme into the right format."
        );

        const previewMode = screen.queryByTestId("react-markdown-mock");
        const button = screen.queryByRole("button");

        expect(previewMode).not.toBeInTheDocument();
        expect(button).not.toBeInTheDocument();
      });
    });

    describe("handles errors with updating readme", () => {
      const existingReadme = "# Hello" as TopicDocumentationMarkdown;
      const userInput = "**Hello world**";

      const originalConsoleError = console.error;
      beforeEach(() => {
        console.error = jest.fn();
        mockUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...mockTopicDetails.topicOverview,
            topicDocumentation: existingReadme,
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

  describe("handles documentation user that are not topic owner", () => {
    describe("if the topic has no documentation yet", () => {
      beforeAll(() => {
        mockUseTopicDetails.mockReturnValue(mockTopicDetailsNotTopicOwner);
        customRender(
          <AquariumContext>
            <TopicDocumentation />
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
  });
});
