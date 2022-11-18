import {
  transformTopicApiResponse,
  transformTopicEnvApiResponse,
} from "src/domain/topic/topic-transformer";
import {
  Topic,
  TopicApiResponse,
  TopicDTOApiResponse,
  TopicEnv,
  TopicEnvDTO,
} from "src/domain/topic/topic-types";
import {
  baseTestObjectMockedTopic,
  createMockTopicApiResponse,
  createMockTopicEnvDTO,
} from "src/domain/topic/topic-test-helper";

describe("topic-transformer.ts", () => {
  describe("'transformTopicApiResponse' transforms API response into list of topics", () => {
    const mockedTopic: Topic = baseTestObjectMockedTopic();

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

  describe("'transformTopicEnvApiResponse' transforms API response into list of environments", () => {
    it("transforms list of 4 envs with 2 unique env values", () => {
      const testInput: TopicEnvDTO[] = [
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.TST),
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.DEV),
      ];

      expect(transformTopicEnvApiResponse(testInput)).toEqual([
        TopicEnv.DEV,
        "TST",
      ]);
    });

    it("transforms list of 3 envs with 1 unique env value", () => {
      const testInput: TopicEnvDTO[] = [
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.DEV),
        createMockTopicEnvDTO(TopicEnv.DEV),
      ];

      expect(transformTopicEnvApiResponse(testInput)).toEqual([TopicEnv.DEV]);
    });
  });
});
