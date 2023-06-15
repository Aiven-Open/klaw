import {
  transformGetTopicAdvancedConfigOptionsResponse,
  transformGetTopicRequestsResponse,
  transformTopicApiResponse,
  transformTopicOverviewResponse,
} from "src/domain/topic/topic-transformer";
import {
  Topic,
  TopicApiResponse,
  TopicRequestApiResponse,
  TopicRequest,
  TopicOverview,
} from "src/domain/topic/topic-types";
import {
  baseTestObjectMockedTopic,
  createMockTopicApiResponse,
} from "src/domain/topic/topic-test-helper";
import { KlawApiResponse } from "types/utils";

describe("topic-transformer.ts", () => {
  describe("'transformTopicApiResponse' transforms API response into list of topics", () => {
    const mockedTopic: Topic = baseTestObjectMockedTopic();

    it("transforms a response with two entries", () => {
      const apiResponse = createMockTopicApiResponse({
        entries: 2,
      });

      const result: TopicApiResponse = {
        totalPages: 1,
        currentPage: 1,
        entries: [mockedTopic, mockedTopic],
      };

      expect(transformTopicApiResponse(apiResponse)).toEqual(result);
    });

    it("transforms a response with four entries", () => {
      const apiResponse = createMockTopicApiResponse({
        entries: 4,
      });
      const result: TopicApiResponse = {
        totalPages: 1,
        currentPage: 1,
        entries: [mockedTopic, mockedTopic, mockedTopic, mockedTopic],
      };

      expect(transformTopicApiResponse(apiResponse)).toStrictEqual(result);
    });

    it("transforms a response with no entries", () => {
      const apiResponse: KlawApiResponse<"getTopics"> = [];
      const result: TopicApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformTopicApiResponse(apiResponse)).toStrictEqual(result);
    });
  });

  describe("transformGetTopicAdvancedConfigOptionsResponse", () => {
    it("transforms an config without known documentation", () => {
      const apiResponse: KlawApiResponse<"getAdvancedTopicConfigs"> = {
        MIN_COMPACTION_LAG_MS: "min.compaction.lag.ms",
      };
      const result = [
        {
          key: "MIN_COMPACTION_LAG_MS",
          name: "min.compaction.lag.ms",
          documentation: {
            link: "https://kafka.apache.org/documentation/#topicconfigs_min.compaction.lag.ms",
            text: "Specify the minimum time a message will remain uncompacted in the log.",
          },
        },
      ];
      expect(
        transformGetTopicAdvancedConfigOptionsResponse(apiResponse)
      ).toStrictEqual(result);
    });
    it("transforms an config without known documentation", () => {
      const apiResponse: KlawApiResponse<"getAdvancedTopicConfigs"> = {
        CONFIG_WITHOUT_DOCUMENTATION: "config.without.documentation",
      };
      const result = [
        {
          key: "CONFIG_WITHOUT_DOCUMENTATION",
          name: "config.without.documentation",
        },
      ];
      expect(
        transformGetTopicAdvancedConfigOptionsResponse(apiResponse)
      ).toStrictEqual(result);
    });
  });

  describe("transformGetTopicRequestsResponse", () => {
    it("transforms empty payload into empty array", () => {
      const transformedResponse = transformGetTopicRequestsResponse([]);

      const result: TopicRequestApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformedResponse).toEqual(result);
    });

    it("transforms all response items into expected type", () => {
      const mockedResponse: TopicRequest[] = [
        {
          topicname: "test-topic-1",
          environment: "1",
          topicpartitions: 4,
          teamname: "NCC1701D",
          remarks: "asap",
          description: "This topic is for test",
          replicationfactor: "2",
          environmentName: "BRG",
          topicid: 1000,
          advancedTopicConfigEntries: [
            {
              configKey: "cleanup.policy",
              configValue: "delete",
            },
          ],
          requestOperationType: "CREATE",
          requestor: "jlpicard",
          requesttime: "1987-09-28T13:37:00.001+00:00",
          requesttimestring: "28-Sep-1987 13:37:00",
          requestStatus: "CREATED",
          totalNoPages: "3",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
          teamId: 1003,
          allPageNos: ["1"],
          currentPage: "1",
          editable: true,
          deletable: true,
          deleteAssociatedSchema: false,
        },
        {
          topicname: "test-topic-2",
          environment: "1",
          topicpartitions: 4,
          teamname: "MIRRORUNIVERSE",
          remarks: "asap",
          description: "This topic is for test",
          replicationfactor: "2",
          environmentName: "SBY",
          topicid: 1001,
          advancedTopicConfigEntries: [
            {
              configKey: "compression.type",
              configValue: "snappy",
            },
          ],

          requestOperationType: "UPDATE",
          requestor: "bcrusher",
          requesttime: "1994-23-05T13:37:00.001+00:00",
          requesttimestring: "23-May-1994 13:37:00",
          requestStatus: "APPROVED",
          totalNoPages: "3",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
          teamId: 1003,
          allPageNos: ["1"],
          currentPage: "1",
          editable: true,
          deletable: true,
          deleteAssociatedSchema: false,
        },
      ];
      const transformedResponse =
        transformGetTopicRequestsResponse(mockedResponse);

      const result: TopicRequestApiResponse = {
        totalPages: 3,
        currentPage: 1,
        entries: mockedResponse,
      };

      expect(transformedResponse.entries).toHaveLength(2);
      expect(transformedResponse).toEqual(result);
    });
  });

  describe("transformTopicOverviewResponse", () => {
    it("transforms topic overview from backend with only required properties", () => {
      const mockedResponse: KlawApiResponse<"getTopicOverview"> = {
        topicExists: false,
        schemaExists: false,
        prefixAclsExists: false,
        txnAclsExists: false,
        topicInfoList: [
          {
            topicName: "test-topic",
            noOfPartitions: 1,
            noOfReplicas: "2",
            teamname: "TEAM",
            teamId: 3,
            envId: "4",
            showEditTopic: false,
            showDeleteTopic: false,
            topicDeletable: false,
            hasOpenRequest: false,
            hasOpenACLRequest: false,
            envName: "DEV",
          },
        ],
        topicPromotionDetails: {
          status: "test",
        },
        availableEnvironments: [],
        topicIdForDocumentation: 1,
      };

      const transformedResponse =
        transformTopicOverviewResponse(mockedResponse);

      const result: TopicOverview = {
        availableEnvironments: [],
        prefixAclsExists: false,
        schemaExists: false,
        topicExists: false,
        topicIdForDocumentation: 1,
        topicInfo: {
          envId: "4",
          envName: "DEV",
          hasOpenACLRequest: false,
          hasOpenRequest: false,
          noOfPartitions: 1,
          noOfReplicas: "2",
          showDeleteTopic: false,
          showEditTopic: false,
          teamId: 3,
          teamname: "TEAM",
          topicDeletable: false,
          topicName: "test-topic",
        },
        topicPromotionDetails: {
          status: "test",
        },
        txnAclsExists: false,
      };

      expect(transformedResponse).toEqual(result);
    });

    it("transforms topic overview from backend with all properties", () => {
      const mockedResponse: KlawApiResponse<"getTopicOverview"> = {
        topicExists: false,
        schemaExists: false,
        prefixAclsExists: false,
        txnAclsExists: false,
        topicInfoList: [
          {
            topicName: "test-topic",
            noOfPartitions: 1,
            noOfReplicas: "2",
            teamname: "TEAM",
            teamId: 3,
            envId: "4",
            showEditTopic: false,
            showDeleteTopic: false,
            topicDeletable: false,
            hasOpenRequest: false,
            hasOpenACLRequest: false,
            envName: "DEV",
          },
        ],
        topicPromotionDetails: {
          status: "test",
        },
        availableEnvironments: [],
        topicIdForDocumentation: 1,
        topicDocumentation: "This is the documentation",
        aclInfoList: [
          {
            req_no: "111",
            aclPatternType: "test1",
            environment: "1",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            showDeleteAcl: false,
            teamid: 1111,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        prefixedAclInfoList: [
          {
            req_no: "222",
            aclPatternType: "test2",
            environment: "2",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            showDeleteAcl: false,
            teamid: 2222,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        transactionalAclInfoList: [
          {
            req_no: "333",
            aclPatternType: "test3",
            environment: "3",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            showDeleteAcl: false,
            teamid: 3333,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        topicHistoryList: [
          {
            environmentName: "TST",
            teamName: "teami",
            requestedBy: "me",
            requestedTime: "2023",
            approvedBy: "them",
            approvedTime: "2024",
            remarks: "no remarks here",
          },
        ],
      };

      const transformedResponse =
        transformTopicOverviewResponse(mockedResponse);

      const result: TopicOverview = {
        availableEnvironments: [],
        prefixAclsExists: false,
        schemaExists: false,
        topicExists: false,
        topicIdForDocumentation: 1,
        topicInfo: {
          envId: "4",
          envName: "DEV",
          hasOpenACLRequest: false,
          hasOpenRequest: false,
          noOfPartitions: 1,
          noOfReplicas: "2",
          showDeleteTopic: false,
          showEditTopic: false,
          teamId: 3,
          teamname: "TEAM",
          topicDeletable: false,
          topicName: "test-topic",
        },
        topicPromotionDetails: {
          status: "test",
        },
        txnAclsExists: false,
        aclInfoList: [
          {
            aclPatternType: "test1",
            environment: "1",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            req_no: "111",
            showDeleteAcl: false,
            teamid: 1111,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        prefixedAclInfoList: [
          {
            aclPatternType: "test2",
            environment: "2",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            req_no: "222",
            showDeleteAcl: false,
            teamid: 2222,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        transactionalAclInfoList: [
          {
            aclPatternType: "test3",
            environment: "3",
            environmentName: "TST",
            kafkaFlavorType: "APACHE_KAFKA",
            req_no: "333",
            showDeleteAcl: false,
            teamid: 3333,
            teamname: "teami",
            topicname: "my-topic",
            topictype: "test",
          },
        ],
        topicDocumentation: "This is the documentation",
        topicHistoryList: [
          {
            approvedBy: "them",
            approvedTime: "2024",
            environmentName: "TST",
            remarks: "no remarks here",
            requestedBy: "me",
            requestedTime: "2023",
            teamName: "teami",
          },
        ],
      };

      expect(transformedResponse).toEqual(result);
    });
  });
});
