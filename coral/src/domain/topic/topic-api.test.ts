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
import { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";
import { Environment } from "src/domain/environment";

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
          status: 200,
          data: { success: true, message: "success" },
        },
      });
    });
    it("calls api.post with correct payload", () => {
      const postSpy = jest.spyOn(api, "post");
      const env: Environment = {
        name: "DEV",
        id: "1",
        type: "kafka",
        params: {},
      };

      const parameters: Schema = {
        description: "this-is-description",
        environment: env,
        remarks: "",
        replicationfactor: "1",
        topicname: "topic-for-unittest",
        topicpartitions: "1",
      };

      const payload: KlawApiRequestQueryParameters<"createTopicsCreateRequest"> =
        {
          environment: "1",
          topicname: "topic-for-unittest",
          topicpartitions: 1,
          replicationfactor: "1",
          advancedTopicConfigEntries: [],
          description: "this-is-description",
          remarks: "",
          requestOperationType: "CREATE" as const,
        };
      requestTopic(parameters);
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
