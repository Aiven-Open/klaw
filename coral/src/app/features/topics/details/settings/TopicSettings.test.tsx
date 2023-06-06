import { TopicSettings } from "src/app/features/topics/details/settings/TopicSettings";
import { cleanup, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Context as AquariumContext } from "@aivenio/aquarium";
import { deleteTopic } from "src/domain/topic";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));

jest.mock("src/domain/topic/topic-api.ts");
const mockDeleteTopic = deleteTopic as jest.MockedFunction<typeof deleteTopic>;

const testTopicName = "my-nice-topic";
const testEnvironmentId = 8;

describe("TopicSettings", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue({
        topicName: testTopicName,
        environmentId: testEnvironmentId,
      });

      customRender(
        <AquariumContext>
          <TopicSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterAll(cleanup);

    it("shows a page headline", () => {
      const pageHeadline = screen.getByRole("heading", { name: "Settings" });

      expect(pageHeadline).toBeVisible();
    });

    it("shows a headline for the danger zone", () => {
      const dangerHeadline = screen.getByRole("heading", {
        name: "Danger zone",
      });

      expect(dangerHeadline).toBeVisible();
    });

    it("shows a headline for delete topic", () => {
      const deleteTopicHeadline = screen.getByRole("heading", {
        name: "Delete this topic",
      });

      expect(deleteTopicHeadline).toBeVisible();
    });

    it("shows a warning text about deletion of the topic", () => {
      const warningText = screen.getByText(
        "Once you delete a topic, there is no going back. Please be certain."
      );

      expect(warningText).toBeVisible();
    });

    it("shows a button to delete the topic", () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      expect(button).toBeVisible();
    });
  });

  describe("enables user to delete a topic", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue({
        topicName: testTopicName,
        environmentId: testEnvironmentId,
      });

      customRender(
        <AquariumContext>
          <TopicSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
      console.error = originalConsoleError;
    });

    it('shows a confirmation modal when user clicks "Delete topic"', async () => {
      const confirmationModalBeforeClick = screen.queryByRole("dialog");
      expect(confirmationModalBeforeClick).not.toBeInTheDocument();

      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);
      const confirmationModal = screen.getByRole("dialog");

      expect(confirmationModal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it('removes modal and does not delete topic if user clicks "cancel"', async () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(dialog).not.toBeInTheDocument();
      expect(mockedNavigate).not.toHaveBeenCalled();
      expect(mockDeleteTopic).not.toHaveBeenCalled();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("deletes topic successfully when user confirms deleting", async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      mockDeleteTopic.mockResolvedValue({ success: true });

      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirmButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(confirmButton);

      expect(mockDeleteTopic).toHaveBeenCalledWith({
        deleteAssociatedSchema: false,
        env: testEnvironmentId,
        topicName: testTopicName,
      });
      expect(mockedNavigate).toHaveBeenCalledWith("/topics");
      await waitForElementToBeRemoved(dialog);
      expect(console.error).not.toHaveBeenCalled();
    });

    it("deletes topic and associates schemas successfully when user confirms deleting", async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      mockDeleteTopic.mockResolvedValue({ success: true });

      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const checkbox = screen.getByRole("checkbox", {
        name: "Delete all versions of schema associated with this topic if a schema exists.",
      });

      await user.click(checkbox);

      const confirmButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(confirmButton);

      expect(mockDeleteTopic).toHaveBeenCalledWith({
        deleteAssociatedSchema: true,
        env: testEnvironmentId,
        topicName: testTopicName,
      });
      expect(mockedNavigate).toHaveBeenCalledWith("/topics");
      await waitForElementToBeRemoved(dialog);
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a message if deleting the topic resulted in an error", async () => {
      mockDeleteTopic.mockRejectedValue({
        success: false,
        message: "Oh no error",
      });

      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirmButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(confirmButton);

      expect(mockDeleteTopic).toHaveBeenCalledWith({
        deleteAssociatedSchema: false,
        env: testEnvironmentId,
        topicName: testTopicName,
      });

      expect(mockedNavigate).not.toHaveBeenCalled();
      await waitForElementToBeRemoved(dialog);

      const errorMessage = screen.getByRole("alert");

      expect(errorMessage).toBeVisible();
      expect(errorMessage).toHaveTextContent("Oh no error");
      expect(console.error).toHaveBeenCalledWith({
        message: "Oh no error",
        success: false,
      });
    });
  });
});
