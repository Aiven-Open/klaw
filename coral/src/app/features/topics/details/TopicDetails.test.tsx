import { Context as AquariumContext } from "@aivenio/aquarium";
import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicOverview, TopicSchemaOverview } from "src/domain/topic";
import {
  requestTopicClaim,
  getSchemaOfTopic,
  getTopicOverview,
} from "src/domain/topic/topic-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockUseParams = jest.fn();
const mockMatches = jest.fn();
const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => mockUseParams(),
  useMatches: () => mockMatches(),
  Navigate: () => mockedNavigate(),
}));

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/topic/topic-api");

const mockGetTopicOverview = getTopicOverview as jest.MockedFunction<
  typeof getTopicOverview
>;
const mockGetSchemaOfTopic = getSchemaOfTopic as jest.MockedFunction<
  typeof getSchemaOfTopic
>;
const mockRequestTopicClaim = requestTopicClaim as jest.MockedFunction<
  typeof requestTopicClaim
>;

const testTopicName = "my-nice-topic";
const testTopicOverview: TopicOverview = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    topicName: testTopicName,
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 0,
    envId: "1",
    clusterId: 6,
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    envName: "DEV",
    hasACL: false,
    hasOpenTopicRequest: false,
    hasOpenACLRequest: false,
    hasOpenClaimRequest: false,
    hasOpenSchemaRequest: false,
    highestEnv: true,
    hasOpenRequest: false,
    hasSchema: false,
    description: "my description",
  },
  aclInfoList: [
    {
      req_no: "1006",
      acl_ssl: "aivtopic3user",
      topicname: "aivtopic3",
      topictype: "Producer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 0,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
    {
      req_no: "1011",
      acl_ssl: "declineme",
      topicname: "aivtopic3",
      topictype: "Producer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 0,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
    {
      req_no: "1060",
      acl_ssl: "amathieu",
      topicname: "aivtopic3",
      topictype: "Producer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 0,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
  ],
  topicHistoryList: [
    {
      environmentName: "DEV",
      teamName: "Ospo",
      requestedBy: "muralibasani",
      requestedTime: "2022-Nov-04 14:41:18",
      approvedBy: "josepprat",
      approvedTime: "2022-Nov-04 14:48:38",
      remarks: "Create",
    },
  ],
  topicPromotionDetails: { status: "SUCCESS" },
  availableEnvironments: [
    {
      id: "1",
      name: "DEV",
    },
    {
      id: "2",
      name: "TST",
    },
  ],
  topicIdForDocumentation: 1,
};

const testTopicSchemas: TopicSchemaOverview = {
  topicExists: true,
  schemaExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  allSchemaVersions: [1],
  latestVersion: 1,
  schemaPromotionDetails: {
    status: "SUCCESS",
    sourceEnv: "3",
    targetEnv: "TST_SCH",
    targetEnvId: "9",
  },
  schemaDetailsPerEnv: {
    id: 2,
    version: 1,
    nextVersion: 0,
    prevVersion: 0,
    compatibility: "BACKWARD",
    content:
      '{\n  "doc" : "example",\n  "fields" : [ {\n    "default" : "6666665",\n    "doc" : "my test number",\n    "name" : "test",\n    "namespace" : "test",\n    "type" : "string"\n  } ],\n  "name" : "example",\n  "namespace" : "example",\n  "type" : "record"\n}',
    env: "DEV",
    showNext: false,
    showPrev: false,
    latest: true,
    promoteOnly: false,
  },
};

describe("TopicDetails", () => {
  const user = userEvent.setup();

  beforeEach(() => {
    mockGetSchemaOfTopic.mockResolvedValue(testTopicSchemas);

    mockUseParams.mockReturnValue({
      topicName: testTopicName,
    });

    mockedNavigate.mockImplementation(() => {
      return <div data-testid={"react-router-navigate"} />;
    });
  });

  describe("fetches the topic overview based on topic name", () => {
    beforeAll(() => {
      mockGetTopicOverview.mockResolvedValue(testTopicOverview);

      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches topic overview and schema data on first load of page", async () => {
      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      await waitFor(() =>
        expect(mockGetTopicOverview).toHaveBeenCalledWith({
          topicName: testTopicName,
          environmentId: testTopicOverview.availableEnvironments[0].id,
        })
      );

      // This is a dependent query relying on mockGetTopicOverview to have finished fetching
      // So we need to await
      await waitFor(() =>
        expect(mockGetSchemaOfTopic).toHaveBeenCalledWith({
          topicName: testTopicName,
          kafkaEnvId: "1",
        })
      );
    });

    it("fetches the data anew when user changes environment", async () => {
      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      const select = await screen.findByRole("combobox", {
        name: "Select environment",
      });

      await user.selectOptions(
        select,
        testTopicOverview.availableEnvironments[1].name
      );

      await waitFor(() =>
        expect(mockGetTopicOverview).toHaveBeenCalledWith({
          topicName: testTopicName,
          environmentId: testTopicOverview.availableEnvironments[1].id,
        })
      );
      // This is a dependent query relying on mockGetTopicOverview to have finished fetching
      // So we need to await
      await waitFor(() => {
        expect(mockGetSchemaOfTopic).toHaveBeenCalledWith({
          topicName: testTopicName,
          kafkaEnvId: testTopicOverview.availableEnvironments[1].id,
        });
      });
    });
  });

  describe("renders the correct tab navigation based on router match", () => {
    beforeEach(() => {
      mockGetTopicOverview.mockResolvedValue(testTopicOverview);
    });
    afterEach(cleanup);

    it("shows the tablist with Overview as currently active panel", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("Overview");
    });

    it("shows the tablist with History as currently active panel", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_history",
        },
      ]);

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("History");
    });
  });

  describe("only renders header and tablist if route is matching defined tabs", () => {
    beforeEach(() => {
      mockGetTopicOverview.mockResolvedValue(testTopicOverview);
    });
    afterEach(cleanup);

    it("does render content if the route matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      const tabList = screen.getByRole("tablist");

      expect(tabList).toBeVisible();
      expect(mockedNavigate).not.toHaveBeenCalled();
    });

    it("redirects user to topic overview if the route does not matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "something",
        },
      ]);

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      const tabList = screen.queryByRole("tablist");

      expect(tabList).not.toBeInTheDocument();

      expect(mockedNavigate).toHaveBeenCalled();
    });
  });

  describe("correctly renders the Claim topic banner", () => {
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("does not renders the Claim topic banner when user is topic owner", async () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
      mockGetTopicOverview.mockResolvedValue(testTopicOverview);

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const description = screen.queryByText(
        "Your team is not the owner of this topic. Click below to create a claim request for this topic."
      );
      const button = screen.queryByRole("button", { name: "Claim topic" });

      expect(description).not.toBeInTheDocument();
      expect(button).not.toBeInTheDocument();
    });

    it("renders the Claim topic banner when user is not topic owner", async () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
      mockGetTopicOverview.mockResolvedValue({
        ...testTopicOverview,
        topicInfo: { ...testTopicOverview.topicInfo, topicOwner: false },
      });

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const description = await waitFor(() =>
        screen.getByText(
          `This topic is currently owned by ${testTopicOverview.topicInfo.teamname}. Select "Claim topic" to request ownership.`
        )
      );
      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      expect(description).toBeVisible();
      expect(button).toBeEnabled();
    });
  });

  describe("allows users to claim a topic when they are not topic owner", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);
      mockGetTopicOverview.mockResolvedValue({
        ...testTopicOverview,
        topicInfo: { ...testTopicOverview.topicInfo, topicOwner: false },
      });

      customRender(
        <AquariumContext>
          <TopicDetails topicName={testTopicName} />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a modal when clicking the Claim topic button", async () => {
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("sends a request when clicking the submit button without entering a message", async () => {
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim topic",
      });

      await userEvent.click(submitButton);

      expect(mockRequestTopicClaim).toHaveBeenCalledWith({
        topicName: testTopicOverview.topicInfo.topicName,
        env: testTopicOverview.topicInfo.envId,
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("sends a request when clicking the submit button with the message entered by the user", async () => {
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim topic",
      });

      const textArea = screen.getByRole("textbox");

      await userEvent.type(textArea, "hello");
      await userEvent.click(submitButton);

      expect(mockRequestTopicClaim).toHaveBeenCalledWith({
        topicName: testTopicOverview.topicInfo.topicName,
        env: testTopicOverview.topicInfo.envId,
        remark: "hello",
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("closes the modal and renders a toast when request was successful", async () => {
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim topic",
      });

      await userEvent.click(submitButton);

      expect(modal).not.toBeVisible();

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith({
          message: "Topic claim request successfully created",
          position: "bottom-left",
          variant: "default",
        })
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("closes the modal displays an alert when request was not successful", async () => {
      const mockErrorMessage = "There was an error";
      await waitForElementToBeRemoved(screen.getByPlaceholderText("Loading"));

      mockRequestTopicClaim.mockRejectedValue(mockErrorMessage);

      const button = await waitFor(() =>
        screen.getByRole("button", { name: "Claim topic" })
      );

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();

      const submitButton = screen.getByRole("button", {
        name: "Request claim topic",
      });

      await userEvent.click(submitButton);

      expect(modal).not.toBeVisible();

      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
      expect(console.error).toHaveBeenCalledWith(mockErrorMessage);
    });
  });
});
