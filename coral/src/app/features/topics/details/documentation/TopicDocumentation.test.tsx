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

const testTopicOverview: TopicOverview = {
  availableEnvironments: [],
  prefixAclsExists: false,
  schemaExists: false,
  txnAclsExists: false,
  topicPromotionDetails: { status: "" },
  topicIdForDocumentation: 99999,
  topicInfo: {
    topicName: "documentation-test-topic",
  } as TopicOverview["topicInfo"],
  topicExists: true,
};

const mockTopicDetails = {
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

  describe("enables user to update documentation", () => {
    const userInput = "**Hello world**";

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

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("enables user to cancel editing the documentation", async () => {
      const addButton = screen.getByRole("button", {
        name: "Add documentation",
      });

      await user.click(addButton);

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

      const noDocumentation = screen.getByRole("heading", {
        name: "No documentation",
      });
      expect(noDocumentation).toBeVisible();
    });

    it("saves documentation when user clicks button", async () => {
      const addButton = screen.getByRole("button", {
        name: "Add documentation",
      });

      await user.click(addButton);

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
        topicDocumentation: userInput,
      });
    });
  });
});
