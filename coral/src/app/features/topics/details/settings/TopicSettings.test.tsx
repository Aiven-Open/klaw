import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicSettings } from "src/app/features/topics/details/settings/TopicSettings";
import { TopicOverview, deleteTopic } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiModel } from "types/utils";

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
const testTopicInfo: KlawApiModel<"TopicOverviewInfo"> = {
  topicName: testTopicName,
  noOfPartitions: 1,
  noOfReplicas: "1",
  teamname: "Ospo",
  teamId: 1003,
  envId: "1",
  clusterId: 6,
  showEditTopic: true,
  showDeleteTopic: true,
  topicDeletable: true,
  envName: "DEV",
  topicOwner: true,
  hasACL: false,
  hasOpenTopicRequest: false,
  hasOpenACLRequest: false,
  highestEnv: true,
  hasOpenRequest: false,
  hasSchema: false,
  description: "my description",
};
const testTopicOverview: TopicOverview = {
  topicExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  schemaExists: false,
  topicInfo: testTopicInfo,
  aclInfoList: [],
  topicHistoryList: [],
  availableEnvironments: [],
  topicPromotionDetails: { status: "STATUS" },
  topicIdForDocumentation: 1,
};

const mockTopicDetails = {
  topicName: "my-nice-topic",
  environmentId: 8,
  topicOverview: testTopicOverview,
  topicOverviewIsRefetching: false,
  topicSchemasIsRefetching: false,
};
describe("TopicSettings", () => {
  const user = userEvent.setup();

  describe("shows information if user is not allowed to delete topic", () => {
    beforeAll(() => {
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverview: {
          ...testTopicOverview,
          topicInfo: { ...testTopicInfo, topicOwner: false },
        },
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

    it("shows no headline for the danger zone", () => {
      const dangerHeadline = screen.queryByRole("heading", {
        name: "Danger zone",
      });

      expect(dangerHeadline).not.toBeInTheDocument();
    });

    it("shows no button to delete the topic", () => {
      const button = screen.queryByRole("button", {
        name: "Delete topic",
      });

      expect(button).not.toBeInTheDocument();
    });

    it("shows information that settings are only available for users of a team", () => {
      const information = screen.getByText(
        "Settings can only be edited by team members of the team the topic does belong" +
          " to."
      );

      expect(information).toBeVisible();
    });
  });

  describe("shows information if user is allowed to delete but topic is not deletable at the moment", () => {
    describe("informs user that topic is not deletable because there are active subscriptions", () => {
      beforeAll(() => {
        mockDeleteTopic.mockImplementation(jest.fn());
        mockedUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...testTopicOverview,
            topicInfo: {
              ...testTopicInfo,
              showDeleteTopic: false,
              hasACL: true,
            },
          },
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

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information topic can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this topic:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that topic has open ACL requests", () => {
        const information = screen.getByText(
          "The topic has active subscriptions. Please delete them before deleting the topic."
        );

        expect(information).toBeVisible();
      });

      it("shows a disabled button to delete the topic", () => {
        const button = screen.getByRole("button", {
          name: "Delete topic",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that topic  is not deletable because it is on a higher environment", () => {
      beforeAll(() => {
        mockDeleteTopic.mockImplementation(jest.fn());
        mockedUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...testTopicOverview,
            topicInfo: {
              ...testTopicInfo,
              showDeleteTopic: false,
              highestEnv: false,
            },
          },
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

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information topic can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this topic:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that topic exists on a higher environment", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The topic is on a higher environment. Please delete the topic from that environment first."
        );
        expect(listItem).toHaveLength(1);
      });

      it("shows a disabled button to delete the topic", () => {
        const button = screen.getByRole("button", {
          name: "Delete topic",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that topic is not deletable because of a pending request", () => {
      beforeAll(() => {
        mockDeleteTopic.mockImplementation(jest.fn());
        mockedUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...testTopicOverview,
            topicInfo: {
              ...testTopicInfo,
              showDeleteTopic: false,
              hasOpenRequest: true,
            },
          },
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

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information topic can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this topic:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that topic has a pending request", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The topic has a pending request."
        );
        expect(listItem).toHaveLength(1);
      });

      it("shows a disabled button to delete the topic", () => {
        const button = screen.getByRole("button", {
          name: "Delete topic",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that topic is not deletable because multiple reasons", () => {
      beforeAll(() => {
        mockDeleteTopic.mockImplementation(jest.fn());
        mockedUseTopicDetails.mockReturnValue({
          ...mockTopicDetails,
          topicOverview: {
            ...testTopicOverview,
            topicInfo: {
              ...testTopicInfo,
              showDeleteTopic: false,
              hasOpenRequest: true,
              hasACL: true,
            },
          },
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

      it("shows information that topic has a pending request and open ACL requests", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The topic has active subscriptions. Please delete them before deleting the topic."
        );

        expect(listItem[1]).toHaveTextContent(
          "The topic has a pending request."
        );

        expect(listItem).toHaveLength(2);
      });
    });
  });

  describe("renders all necessary elements if user can delete topic and it is deletable", () => {
    beforeAll(() => {
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue(mockTopicDetails);

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

  describe("shows information about refetching state", () => {
    beforeAll(() => {
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue({
        ...mockTopicDetails,
        topicOverviewIsRefetching: true,
        topicOverview: {
          ...testTopicOverview,
          topicInfo: {
            ...testTopicInfo,
            showDeleteTopic: false,
            hasOpenACLRequest: true,
          },
        },
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

    it("shows a SR only text", () => {
      const loadingInformation = screen.getByText("Loading information");

      expect(loadingInformation).toBeVisible();
      expect(loadingInformation).toHaveClass("visually-hidden");
    });

    it("does not show information why user can not delete request", () => {
      const noDeleteText = screen.queryByText(
        "You can not create a delete request for this topic"
      );

      expect(noDeleteText).not.toBeInTheDocument();
    });

    it("does not show headline and text with information about deletion", () => {
      const deleteHeadline = screen.queryByText("Delete this topic");
      const deleteInformation = screen.queryByText(
        "Once you delete a topic, there is no going back. Please be certain."
      );

      expect(deleteHeadline).not.toBeInTheDocument();
      expect(deleteInformation).not.toBeInTheDocument();
    });

    it("disables the button to delete a topic", () => {
      const deleteButton = screen.getByRole("button", {
        name: "Delete topic",
        hidden: true,
      });

      expect(deleteButton).toBeDisabled();
    });
  });

  describe("enables user to delete a topic", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();
      mockDeleteTopic.mockImplementation(jest.fn());
      mockedUseTopicDetails.mockReturnValue(mockTopicDetails);

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
      expect(dialog).not.toBeVisible();
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
      expect(dialog).not.toBeVisible();
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
      expect(dialog).not.toBeVisible();

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
