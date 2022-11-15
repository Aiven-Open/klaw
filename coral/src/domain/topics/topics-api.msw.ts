import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";
import { TopicDTOApiResponse } from "src/domain/topics/topics-types";
import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";
import { createMockTopicApiResponse } from "src/domain/topics/topic-test-helper";

function mockTopicGetRequest({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error" | "empty";
}) {
  mswInstance.use(
    rest.get("getTopics", async (req, res, ctx) => {
      // error path
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }
      // response empty
      else if (scenario === "empty") {
        return res(ctx.status(200), ctx.json([]));
      }
      // success part
      return res(ctx.status(200), ctx.json(mockedResponse));
    })
  );
}

const mockedResponse: TopicDTOApiResponse = createMockTopicApiResponse({
  entries: 10,
});

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(mockedResponse);

export { mockTopicGetRequest, mockedResponseTransformed };
