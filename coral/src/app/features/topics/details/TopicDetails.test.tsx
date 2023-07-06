import { cleanup, screen, waitFor } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicOverview } from "src/domain/topic";
import { getSchemaOfTopic, getTopicOverview } from "src/domain/topic/topic-api";
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

jest.mock("src/domain/topic/topic-api");

const mockGetTopicOverview = getTopicOverview as jest.MockedFunction<
  typeof getTopicOverview
>;
const mockGetSchemaOfTopic = getSchemaOfTopic as jest.MockedFunction<
  typeof getSchemaOfTopic
>;

const testTopicName = "my-nice-topic";
const testTopicOverview: TopicOverview = {
  topicExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    topicName: testTopicName,
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 0,
    envId: "1",
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    envName: "DEV",
    hasACL: false,
    hasOpenTopicRequest: false,
    hasOpenACLRequest: false,
    highestEnv: true,
    hasOpenRequest: false,
    hasSchema: false,
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
  topicPromotionDetails: { status: "STATUS" },
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
const testTopicSchemas = {
  topicExists: true,
  schemaExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  allSchemaVersions: [1],
  latestVersion: 1,
  schemaPromotionDetails: {
    status: "success",
    sourceEnv: "3",
    targetEnv: "TST_SCH",
    targetEnvId: "9",
  },
  schemaDetails: [
    {
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
    },
  ],
};

describe("TopicDetails", () => {
  const user = userEvent.setup();

  beforeEach(() => {
    mockGetTopicOverview.mockResolvedValue(testTopicOverview);
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
      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });
      expect(mockGetTopicOverview).toHaveBeenCalledWith({
        topicName: testTopicName,
        environmentId: undefined,
      });

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
      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });

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
    afterEach(cleanup);

    it("shows the tablist with Overview as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("Overview");
    });

    it("shows the tablist with History as currently active panel", () => {
      mockMatches.mockImplementationOnce(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_history",
        },
      ]);

      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.getByRole("tablist");
      const activeTab = within(tabList).getByRole("tab", { selected: true });

      expect(tabList).toBeVisible();
      expect(activeTab).toHaveAccessibleName("History");
    });
  });

  describe("only renders header and tablist if route is matching defined tabs", () => {
    afterEach(cleanup);

    it("does render content if the route matches an existing tab", () => {
      mockMatches.mockImplementation(() => [
        {
          id: "TOPIC_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });

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

      customRender(<TopicDetails topicName={testTopicName} />, {
        memoryRouter: true,
        queryClient: true,
      });

      const tabList = screen.queryByRole("tablist");

      expect(tabList).not.toBeInTheDocument();

      expect(mockedNavigate).toHaveBeenCalled();
    });
  });
});
