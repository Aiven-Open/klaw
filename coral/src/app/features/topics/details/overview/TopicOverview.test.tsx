import { TopicOverview } from "src/app/features/topics/details/overview/TopicOverview";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  ClusterDetails as ClusterDetailsType,
  getClusterDetails,
} from "src/domain/cluster";
import { cleanup, RenderResult, screen } from "@testing-library/react";
import { getDefinitionList } from "src/services/test-utils/custom-queries";
import { within } from "@testing-library/react/pure";

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));

jest.mock("src/domain/cluster/cluster-api.ts");

const mockGetClusterDetails = getClusterDetails as jest.MockedFunction<
  typeof getClusterDetails
>;

const mockUseTopicDetailsDataWithPromotion = {
  topicName: "aivendemotopic",
  environmentId: "1",
  topicOverview: {
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
      envName: "DEV",
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
      status: "SUCCESS",
      sourceEnv: "1",
      targetEnv: "TST",
      targetEnvId: "2",
      topicName: "aivendemotopic",
    },
  },
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
        status: "SUCCESS",
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

const mockUseTopicDetailsDataWithoutPromotion = {
  topicName: "aivendemotopic",
  environmentId: "1",
  topicOverview: {
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
      envName: "DEV",
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
      status: "SUCCESS",
      sourceEnv: "1",
      targetEnv: "TST",
      targetEnvId: "2",
      topicName: "aivendemotopic",
    },
  },
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
const mockClusterDetails: ClusterDetailsType = {
  allPageNos: [],
  bootstrapServers: "kafka-112233aa-dev-sandbeach.aivencloud.com:12345",
  clusterId: 999,
  clusterName: "DEV",
  clusterType: "Kafka",
  kafkaFlavor: "Aiven for Kafka",
  protocol: "SSL",
  showDeleteCluster: false,
  totalNoPages: "2",
  clusterStatus: "NOT_KNOWN",
};

describe("TopicOverview", () => {
  describe("with promotion banner", () => {
    let component: RenderResult;

    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue(
        mockUseTopicDetailsDataWithPromotion
      );
      mockGetClusterDetails.mockResolvedValue(mockClusterDetails);

      component = customRender(<TopicOverview />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders correct DOM according to data from useTopicDetails and getClusterDetails", () => {
      expect(component.container).toMatchSnapshot();
    });

    it("renders the topic promotion banner", () => {
      const promotionBanner = screen.getByTestId("topic-promotion-banner");
      const promotionInfo = within(promotionBanner).getByText(
        "This topic has not yet been promoted to the TST environment."
      );

      expect(promotionBanner).toBeVisible();
      expect(promotionInfo).toBeVisible();
    });

    it("renders cluster details", () => {
      const clusterDefinitionList = getDefinitionList(component);

      expect(clusterDefinitionList).toBeVisible();
    });
  });

  describe("without promotion banner", () => {
    let component: RenderResult;
    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue(
        mockUseTopicDetailsDataWithoutPromotion
      );
      mockGetClusterDetails.mockResolvedValue(mockClusterDetails);

      component = customRender(<TopicOverview />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders correct DOM according to data from useTopicDetails and getClusterDetails", () => {
      expect(component.container).toMatchSnapshot();
    });

    it("renders no topic promotion banner", () => {
      const promotionBanner = screen.queryByTestId("topic-promotion-banner");

      expect(promotionBanner).not.toBeInTheDocument();
    });

    it("renders cluster details", () => {
      const clusterDefinitionList = getDefinitionList(component);

      expect(clusterDefinitionList).toBeVisible();
    });
  });
});
