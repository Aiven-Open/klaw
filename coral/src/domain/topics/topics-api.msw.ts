import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";
import { TopicDTOApiResponse } from "src/domain/topics/topics-types";
import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";
import { createMockTopicApiResponse } from "src/domain/topics/topic-test-helper";

// pageNo=1
function mockTopicGetRequest({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error" | "empty" | "multiple-pages";
}) {
  mswInstance.use(
    rest.get("getTopics", async (req, res, ctx) => {
      const currentPage = req.url.searchParams.get("pageNo");

      // error path
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }
      // response empty
      else if (scenario === "empty") {
        return res(ctx.status(200), ctx.json([]));

        // response total pages 4, current page 2
      } else if (scenario === "multiple-pages") {
        return res(ctx.status(200), ctx.json(mockedResponseMultiplePage));
      }
      if (currentPage) {
        return res(
          ctx.status(200),
          ctx.json(
            createMockTopicApiResponse({
              entries: 10,
              currentPage: Number(currentPage),
              totalPages: 10,
            })
          )
        );
      }
      return res(ctx.status(200), ctx.json(mockedResponseSinglePage));
    })
  );
}

const mockedResponseSinglePage: TopicDTOApiResponse =
  createMockTopicApiResponse({
    entries: 10,
  });

const mockedResponseMultiplePage: TopicDTOApiResponse =
  createMockTopicApiResponse({
    entries: 2,
    totalPages: 4,
    currentPage: 2,
  });

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(
  mockedResponseSinglePage
);

export { mockTopicGetRequest, mockedResponseTransformed };
