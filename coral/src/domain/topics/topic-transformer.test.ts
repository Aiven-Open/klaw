import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";
import {
  Topic,
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topics/topics-types";
import {
  baseTestObjectMockedTopic,
  createMockTopicApiResponse,
} from "src/domain/topics/topic-test-helper";

describe("topic-transformer.ts", () => {
  const mockedTopic: Topic = baseTestObjectMockedTopic();

  describe("transforms API response into components of topics", () => {
    it("transforms a response with two entries", () => {
      const apiResponse: TopicDTOApiResponse = createMockTopicApiResponse({
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
      const apiResponse: TopicDTOApiResponse = createMockTopicApiResponse({
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
      const apiResponse: TopicDTOApiResponse = [];
      const result: TopicApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformTopicApiResponse(apiResponse)).toStrictEqual(result);
    });
  });
});
