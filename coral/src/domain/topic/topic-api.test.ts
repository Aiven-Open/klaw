import {
  getTopicRequestsForApprover,
  requestTopic,
} from "src/domain/topic/topic-api";
import { server } from "src/services/api-mocks/server";
import api from "src/services/api";
import {
  mockGetTopicRequests,
  mockRequestTopic,
} from "src/domain/topic/topic-api.msw";
import { createMockTopicRequestApiResource } from "src/domain/topic/topic-test-helper";

describe("topic-api", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("requestTopic", () => {
    beforeEach(() => {
      mockRequestTopic({
        mswInstance: server,
        response: {
          data: {
            status: "200 OK",
          },
        },
      });
    });
    it("calls api.post with correct payload", () => {
      const postSpy = jest.spyOn(api, "post");
      const payload = {
        environment: "DEV",
        topicname: "topic-for-unittest",
        topicpartitions: 1,
        replicationfactor: "1",
        advancedTopicConfigEntries: [],
        description: "this-is-description",
        remarks: "",
        topictype: "Create" as const,
      };
      requestTopic(payload);
      expect(postSpy).toHaveBeenCalledTimes(1);
      expect(postSpy).toHaveBeenCalledWith("/createTopics", payload);
    });
  });

  describe("getTopicRequests", () => {
    beforeEach(() => {
      mockGetTopicRequests({
        mswInstance: server,
        response: {
          data: [createMockTopicRequestApiResource()],
        },
      });
    });
    it("calls api.get with correct URL", async () => {
      const getSpy = jest.spyOn(api, "get");
      await getTopicRequestsForApprover({ requestStatus: "created" });
      expect(getSpy).toHaveBeenCalledTimes(1);
      expect(getSpy).toHaveBeenCalledWith(
        "/getTopicRequestsForApprover?pageNo=1&currentPage=1&requestsType=created"
      );
    });
  });
});
