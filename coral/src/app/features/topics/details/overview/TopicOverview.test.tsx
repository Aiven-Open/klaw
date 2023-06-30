import { TopicOverview } from "src/app/features/topics/details/overview/TopicOverview";

import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));

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
      status: "success",
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

describe("TopicOverview", () => {
  it("renders correct DOM according to data from useTopicDetails (with promotion banner)", () => {
    mockedUseTopicDetails.mockReturnValue(mockUseTopicDetailsDataWithPromotion);
    const result = customRender(<TopicOverview />, { memoryRouter: true });
    expect(result).toMatchSnapshot();
  });
  it("renders correct DOM according to data from useTopicDetails (without promotion banner)", () => {
    mockedUseTopicDetails.mockReturnValue(
      mockUseTopicDetailsDataWithoutPromotion
    );
    const result = customRender(<TopicOverview />, { memoryRouter: true });
    expect(result).toMatchSnapshot();
  });
});
