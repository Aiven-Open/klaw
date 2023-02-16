import {
  transformgetTopicAdvancedConfigOptionsResponse,
  transformGetTopicRequestsForApproverResponse,
  transformTopicApiResponse,
} from "src/domain/topic/topic-transformer";
import {
  Topic,
  TopicApiResponse,
  TopicRequestApiResponse,
  TopicRequestStatus,
  TopicRequestTypes,
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
      const apiResponse: KlawApiResponse<"topicsGet"> = [];
      const result: TopicApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformTopicApiResponse(apiResponse)).toStrictEqual(result);
    });
  });

  describe("transformgetTopicAdvancedConfigOptionsResponse", () => {
    it("transforms an config without known documenation", () => {
      const apiResponse: KlawApiResponse<"topicAdvancedConfigGet"> = {
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
        transformgetTopicAdvancedConfigOptionsResponse(apiResponse)
      ).toStrictEqual(result);
    });
    it("transforms an config without known documenation", () => {
      const apiResponse: KlawApiResponse<"topicAdvancedConfigGet"> = {
        CONFIG_WITHOUT_DOCUMENTATION: "config.without.documentation",
      };
      const result = [
        {
          key: "CONFIG_WITHOUT_DOCUMENTATION",
          name: "config.without.documentation",
        },
      ];
      expect(
        transformgetTopicAdvancedConfigOptionsResponse(apiResponse)
      ).toStrictEqual(result);
    });
  });

  describe("transformGetTopicRequestsForApproverResponse", () => {
    it("transforms empty payload into empty array", () => {
      const transformedResponse = transformGetTopicRequestsForApproverResponse(
        []
      );

      const result: TopicRequestApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformedResponse).toEqual(result);
    });

    it("transforms all response items into expected type", () => {
      const mockedResponse = [
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
          topictype: "Create" as TopicRequestTypes,
          requestor: "jlpicard",
          requesttime: "1987-09-28T13:37:00.001+00:00",
          requesttimestring: "28-Sep-1987 13:37:00",
          topicstatus: "created" as TopicRequestStatus,
          totalNoPages: "3",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
          teamId: 1003,
          allPageNos: ["1"],
          currentPage: "1",
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

          topictype: "Update" as TopicRequestTypes,
          requestor: "bcrusher",
          requesttime: "1994-23-05T13:37:00.001+00:00",
          requesttimestring: "23-May-1994 13:37:00",
          topicstatus: "approved" as TopicRequestStatus,
          totalNoPages: "3",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
          teamId: 1003,
          allPageNos: ["1"],
          currentPage: "1",
        },
      ];
      const transformedResponse =
        transformGetTopicRequestsForApproverResponse(mockedResponse);

      const result: TopicRequestApiResponse = {
        totalPages: 3,
        currentPage: 1,
        entries: mockedResponse,
      };

      expect(transformedResponse.entries).toHaveLength(2);
      expect(transformedResponse).toEqual(result);
    });
  });
});
