import { TopicOverview } from "src/app/features/topics/details/overview/TopicOverview";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { TopicOverview as TopicOverviewTyp } from "src/domain/topic";

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));

const topicOverviewWithPromotion: TopicOverviewTyp = {
  prefixAclsExists: false,
  topicIdForDocumentation: 0,
  txnAclsExists: false,
  topicExists: true,
  schemaExists: false,
  topicInfo: {
    topicName: "aivendemotopic",
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 1003,
    envId: "1",
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    hasOpenRequest: false,
    hasOpenTopicRequest: false,
    hasOpenACLRequest: false,
    hasACL: false,
    hasSchema: false,
    envName: "DEV",
    description: "",
    topicOwner: true,
  },
  aclInfoList: [
    {
      req_no: "1061",
      aclPatternType: "LITERAL",
      environment: "1",
      environmentName: "DEV",
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
      showDeleteAcl: true,
      teamid: 1003,
      teamname: "Ospo",
      topicname: "aivendemotopic",
      topictype: "Producer",
      acl_ssl: "aivendemotopicuser",
      consumergroup: "-na-",
    },
  ],
  topicHistoryList: [
    {
      environmentName: "DEV",
      teamName: "Ospo",
      requestedBy: "muralibasani",
      requestedTime: "2023-May-17 10:13:47",
      approvedBy: "josepprat",
      approvedTime: "2023-May-17 10:14:01",
      remarks: "Create",
    },
  ],
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
  topicPromotionDetails: {
    status: "success",
    sourceEnv: "1",
    targetEnv: "TST",
    targetEnvId: "2",
    topicName: "aivendemotopic",
  },
};

const mockUseTopicDetailsDataWithPromotion = {
  topicOverviewIsRefetching: false,
  topicSchemasIsRefetching: false,
  topicName: "aivendemotopic",
  environmentId: "1",
  topicSchemas: {
    topicExists: true,
    schemaExists: true,

    allSchemaVersions: {
      DEV: [1],
    },
    latestVersion: {
      DEV: 1,
    },
    schemaPromotionDetails: {
      DEV: {
        status: "success",
        sourceEnv: "3",
        targetEnv: "TST_SCH",
        targetEnvId: "9",
      },
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
  },
  topicOverview: topicOverviewWithPromotion,
};

const topicOverviewWithoutPromotion: TopicOverviewTyp = {
  prefixAclsExists: false,
  topicIdForDocumentation: 0,
  txnAclsExists: false,
  topicExists: true,
  schemaExists: false,
  topicInfo: {
    topicName: "aivendemotopic",
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 1003,
    envId: "1",
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    hasOpenRequest: false,
    hasOpenTopicRequest: false,
    hasOpenACLRequest: false,
    hasACL: false,
    hasSchema: false,
    topicOwner: false,
    envName: "DEV",
    description: "",
  },
  aclInfoList: [
    {
      req_no: "1061",
      aclPatternType: "LITERAL",
      environment: "1",
      environmentName: "DEV",
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
      showDeleteAcl: true,
      teamid: 1003,
      teamname: "Ospo",
      topicname: "aivendemotopic",
      topictype: "Producer",
      acl_ssl: "aivendemotopicuser",
      consumergroup: "-na-",
    },
  ],
  topicHistoryList: [
    {
      environmentName: "DEV",
      teamName: "Ospo",
      requestedBy: "muralibasani",
      requestedTime: "2023-May-17 10:13:47",
      approvedBy: "josepprat",
      approvedTime: "2023-May-17 10:14:01",
      remarks: "Create",
    },
  ],
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
  topicPromotionDetails: {
    status: "success",
    sourceEnv: "1",
    targetEnv: "TST",
    targetEnvId: "2",
    topicName: "aivendemotopic",
  },
};
const mockUseTopicDetailsDataWithoutPromotion = {
  topicName: "aivendemotopic",
  environmentId: "1",
  topicOverview: topicOverviewWithoutPromotion,
  topicSchemas: {
    topicExists: true,
    schemaExists: true,
    allSchemaVersions: {
      DEV: [1],
    },
    latestVersion: {
      DEV: 1,
    },
    schemaPromotionDetails: {
      DEV: {
        status: "NO_PROMOTION",
        sourceEnv: "3",
        targetEnv: "TST_SCH",
        targetEnvId: "9",
      },
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
  },
};

describe("TopicOverview", () => {
  describe("renders all necessary elements with promotion banner", () => {
    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue(
        mockUseTopicDetailsDataWithPromotion
      );
      customRender(<TopicOverview />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders correct DOM according to data from useTopicDetails", () => {
      expect(screen).toMatchSnapshot();
    });

    it("shows a link to promote topic", () => {
      const link = screen.getByRole("link", { name: "Promote" });

      expect(link).toBeEnabled();
    });

    it("does not show any loading information when all data is up to date", () => {
      const loadingInformation = screen.queryByText("Loading information");

      expect(loadingInformation).not.toBeInTheDocument();
    });
  });

  describe("renders all necessary elements without promotion banner", () => {
    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue(
        mockUseTopicDetailsDataWithoutPromotion
      );
      customRender(<TopicOverview />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders correct DOM according to data from useTopicDetails", () => {
      expect(screen).toMatchSnapshot();
    });

    it("does not show any loading information when all data is up to date", () => {
      const loadingInformation = screen.queryByText("Loading information");

      expect(loadingInformation).not.toBeInTheDocument();
    });
  });

  describe("handles loading state when data is updating", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows loading information for topic stats when topic overview is refetching", () => {
      mockedUseTopicDetails.mockReturnValue({
        ...mockUseTopicDetailsDataWithPromotion,
        topicOverviewIsRefetching: true,
      });

      customRender(<TopicOverview />, { memoryRouter: true });

      const loadingInformation = screen.getAllByText("Loading information");

      expect(loadingInformation).toHaveLength(4);
    });

    it("does not show promotion banner when topic overview is refetching", () => {
      mockedUseTopicDetails.mockReturnValue({
        ...mockUseTopicDetailsDataWithPromotion,
        topicOverviewIsRefetching: true,
      });

      customRender(<TopicOverview />, { memoryRouter: true });
      const link = screen.queryByRole("link", { name: "Promote" });

      expect(link).not.toBeInTheDocument();
    });

    it("shows loading information for schema stats when topic schema is refetching", () => {
      mockedUseTopicDetails.mockReturnValue({
        ...mockUseTopicDetailsDataWithPromotion,
        topicSchemasIsRefetching: true,
      });

      customRender(<TopicOverview />, { memoryRouter: true });
      const loadingInformation = screen.getByText("Loading information");

      expect(loadingInformation).toBeVisible();
    });
  });
});
