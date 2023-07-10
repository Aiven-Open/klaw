import { getTopicStats } from "src/app/features/topics/details/utils";
import { TopicOverview } from "src/domain/topic";

const mockUseTopicDetailsDataWithAcl: TopicOverview = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    topicName: "aivendemotopic",
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 1003,
    envId: "1",
    clusterId: 5,
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
    description: "my description",
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
  topicPromotionDetails: { status: "SUCCESS" },
  topicIdForDocumentation: 1,
};

const mockUseTopicDetailsDataWithoutAcl: TopicOverview = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    topicName: "aivendemotopic",
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 1003,
    envId: "1",
    clusterId: 3,
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
    description: "my description",
  },
  aclInfoList: [],
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
  topicPromotionDetails: { status: "SUCCESS" },
  topicIdForDocumentation: 1,
};

describe("getTopicStats", () => {
  it("should return correct stats from Topic overview data (some ACL data)", () => {
    const results = getTopicStats(mockUseTopicDetailsDataWithAcl);
    const expected = {
      producers: 1,
      consumers: 0,
      partitions: 1,
      replicas: 1,
    };

    expect(results).toStrictEqual(expected);
  });

  it("should return correct stats from Topic overview data (no ACL data)", () => {
    const results = getTopicStats(mockUseTopicDetailsDataWithoutAcl);
    const expected = {
      producers: 0,
      consumers: 0,
      partitions: 1,
      replicas: 1,
    };

    expect(results).toStrictEqual(expected);
  });
});
