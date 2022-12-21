import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import {
  createMockTopic,
  createMockTopicApiResponse,
} from "src/domain/topic/topic-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";

import { isObject } from "lodash";
import { KlawApiResponse } from "types/utils";
// @TODO
// create visible mocked responses and easy responses for different scenarios to use in tests
// use json files from real api for mocked responses for web worker (more realistic(
// don't use the same mocks for both, it gets confusing to maintain
function mockTopicGetRequest({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response?: {
    status?: number;
    data: KlawApiResponse<"topicsGet"> | { message: string };
  };
}) {
  const base = getHTTPBaseAPIUrl();
  mswInstance.use(
    rest.get(`${base}/getTopics`, async (req, res, ctx) => {
      const currentPage = req.url.searchParams.get("pageNo");
      const env = req.url.searchParams.get("env");
      const team = req.url.searchParams.get("teamName");
      const search = req.url.searchParams.get("topicnamesearch");

      if (isObject(response)) {
        return res(ctx.status(response.status ?? 200), ctx.json(response.data));
      }

      // "DEV"
      if (env === "1") {
        return res(ctx.status(200), ctx.json(mockedResponseTopicEnv));
      }

      if (team === "TEST_TEAM_02") {
        return res(ctx.status(200), ctx.json(mockedResponseTopicTeam));
      }

      if (search) {
        return res(ctx.status(200), ctx.json(mockedResponseSearch));
      }

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
    })
  );
}

const mockedResponseSinglePage: KlawApiResponse<"topicsGet"> =
  createMockTopicApiResponse({
    entries: 10,
  });

const mockedResponseMultiplePage: KlawApiResponse<"topicsGet"> =
  createMockTopicApiResponse({
    entries: 2,
    totalPages: 4,
    currentPage: 2,
  });

const mockedResponseMultiplePageTransformed = transformTopicApiResponse(
  mockedResponseMultiplePage
);

const mockedResponseTopicEnv: KlawApiResponse<"topicsGet"> = [
  [
    createMockTopic({
      topicName: "Topic 1",
      topicId: 1,
      environmentsList: ["DEV"],
    }),
    createMockTopic({
      topicName: "Topic 2",
      topicId: 2,
      environmentsList: ["DEV"],
    }),
    createMockTopic({
      topicName: "Topic 3",
      topicId: 3,
      environmentsList: ["DEV"],
    }),
  ],
];

const mockedResponseTopicTeam = [
  [
    {
      ...createMockTopic({
        topicName: "Topic 1",
        topicId: 1,
      }),
      teamname: "TEST_TEAM_02",
    },
    {
      ...createMockTopic({
        topicName: "Topic 2",
        topicId: 2,
      }),
      teamname: "TEST_TEAM_02",
    },
  ],
];

const mockedResponseSearch = [
  [
    {
      ...createMockTopic({
        topicName: "Searched for topic 1",
        topicId: 1,
      }),
      teamname: "TEST_TEAM_01",
    },
    {
      ...createMockTopic({
        topicName: "Searched for topic 2",
        topicId: 2,
      }),
      teamname: "TEST_TEAM_02",
    },
  ],
];

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(
  mockedResponseSinglePage
);

export {
  mockTopicGetRequest,
  mockedResponseTransformed,
  mockedResponseMultiplePageTransformed,
  mockedResponseSinglePage,
  mockedResponseMultiplePage,
  mockedResponseTopicEnv,
};
