import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { Topic, TopicApiResponse } from "src/domain/topic/topic-types";
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
});
