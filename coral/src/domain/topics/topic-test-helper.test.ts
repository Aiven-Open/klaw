import {
  createMockTopic,
  createMockTopicApiResponse,
  baseTestObjectMockedTopic,
} from "src/domain/topics/topic-test-helper";

describe("topic-test-helper.ts", () => {
  describe("createMockTopic", () => {
    const testObject = {
      ...baseTestObjectMockedTopic(),
      topicName: "mocked name",
      topicid: 1,
    };

    it("creates a topic mock with given name and id", () => {
      expect(
        createMockTopic({ topicName: "mocked name", topicId: 1 })
      ).toMatchObject(testObject);
    });
  });

  describe("createMockTopicApiResponse", () => {
    it("creates a mocked api response for topics with 2 entries with current page 1 per default", () => {
      const result = [
        [
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            topicName: `Mocked topic 0`,
            topicid: 0,
            currentPage: "1",
          }),
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            topicName: `Mocked topic 1`,
            topicid: 1,
            currentPage: "1",
          }),
        ],
      ];

      expect(createMockTopicApiResponse({ entries: 2 })).toEqual(result);
    });

    it("creates a mocked api response for topics with 3 entries", () => {
      const result = [
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
      ];

      expect(createMockTopicApiResponse({ entries: 3 })).toEqual(result);
    });

    it("creates a mocked api response for topics with 4 entries", () => {
      const result = [
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
        [expect.objectContaining(baseTestObjectMockedTopic())],
      ];

      expect(createMockTopicApiResponse({ entries: 4 })).toEqual(result);
    });

    it("creates a mocked api response for topics with 6 entries", () => {
      const result = [
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
      ];

      expect(createMockTopicApiResponse({ entries: 6 })).toEqual(result);
    });

    it("creates a mocked api response for topics with 10 entries", () => {
      const result = [
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
        [
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
          expect.objectContaining(baseTestObjectMockedTopic()),
        ],
        [expect.objectContaining(baseTestObjectMockedTopic())],
      ];

      expect(createMockTopicApiResponse({ entries: 10 })).toEqual(result);
    });

    it("creates a mocked api response for topics with 2 entries, 10 total pages", () => {
      const result = [
        [
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "10",
          }),
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "10",
          }),
        ],
      ];

      expect(
        createMockTopicApiResponse({ entries: 2, totalPages: 10 })
      ).toEqual(result);
    });

    it("creates a mocked api response for topics with 2 entries, 3 total pages and 2 as current page", () => {
      const result = [
        [
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "3",
            currentPage: "2",
          }),
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "3",
            currentPage: "2",
          }),
        ],
      ];

      expect(
        createMockTopicApiResponse({
          entries: 2,
          totalPages: 3,
          currentPage: 2,
        })
      ).toEqual(result);
    });

    it("sets the total pages at least to currentPage if is is by mistake less than that", () => {
      const result = [
        [
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "2",
            currentPage: "2",
          }),
          expect.objectContaining({
            ...baseTestObjectMockedTopic(),
            totalNoPages: "2",
            currentPage: "2",
          }),
        ],
      ];

      expect(
        createMockTopicApiResponse({
          entries: 2,
          totalPages: 1,
          currentPage: 2,
        })
      ).toEqual(result);
    });
  });
});
