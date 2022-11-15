import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";
import {
  Topic,
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topics/topics-types";
import { createMockTopicApiResponse } from "src/domain/topics/topic-test-helper";

describe("topic-transformer.ts", () => {
  const mockedTopic: Topic = {
    topicid: expect.any(Number),
    sequence: "341",
    totalNoPages: "1",
    currentPage: "1",
    allPageNos: ["1"],
    topicName: expect.stringContaining("Mock topic"),
    noOfPartitions: 2,
    description: "Topic description",
    documentation: null,
    noOfReplcias: "2",
    teamname: "DevRel",
    cluster: "1",
    clusterId: null,
    environmentsList: ["DEV"],
    showEditTopic: false,
    showDeleteTopic: false,
    topicDeletable: false,
  };
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
