import {
  getTopicRequestsForApprover,
  requestTopic,
} from "src/domain/topic/topic-api";
import { server } from "src/services/api-mocks/server";
import api from "src/services/api";
import {
  mockGetTopicRequestsForApprover,
  mockRequestTopic,
} from "src/domain/topic/topic-api.msw";
import { KlawApiRequestQueryParameters } from "types/utils";

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
      mockGetTopicRequestsForApprover({
        mswInstance: server,
        response: {
          data: [],
        },
      });
    });
    it("calls api.get with correct arguments", async () => {
      const getSpy = jest.spyOn(api, "get");
      const params: Pick<
        KlawApiRequestQueryParameters<"getTopicRequestsForApprover">,
        "pageNo" | "requestStatus"
      > = {
        pageNo: "1",
        requestStatus: "CREATED",
      };
      await getTopicRequestsForApprover(params);
      expect(getSpy).toHaveBeenCalledTimes(1);
      expect(getSpy).toHaveBeenCalledWith(
        "/getTopicRequestsForApprover",
        new URLSearchParams(params)
      );
    });
  });
});
