import {
  transformgetTopicAdvancedConfigOptionsResponse,
  transformGetTopicRequestsResponse,
  transformTopicApiResponse,
} from "src/domain/topic/topic-transformer";
import { Topic, TopicApiResponse } from "src/domain/topic/topic-types";
import {
  baseTestObjectMockedTopic,
  createMockTopicApiResponse,
  createMockTopicRequestApiResource,
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

  describe("transformGetTopicRequestsResponse", () => {
    it("transforms empty payload into empty array", () => {
      const transformedResponse = transformGetTopicRequestsResponse([]);
      expect(transformedResponse).toEqual([]);
    });

    it("transforms all response items into expected type", () => {
      const transformedResponse = transformGetTopicRequestsResponse([
        createMockTopicRequestApiResource({ topicname: "this-is-topic-name" }),
      ]);
      expect(transformedResponse).toHaveLength(1);
      const topicRequest = transformedResponse[0];
      expect(topicRequest).toEqual({ topicName: "this-is-topic-name" });
    });
  });
});
