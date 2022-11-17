import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";
import { TopicDTOApiResponse, TopicEnv } from "src/domain/topics/topics-types";
import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";
import {
  createMockTopic,
  createMockTopicApiResponse,
  createMockTopicEnvDTO,
} from "src/domain/topics/topic-test-helper";

// @TODO
// create visible mocked responses and easy responses for different scenarios to use in tests
// use json files from real api for mocked responses for web worker (more realistic(
// don't use the same mocks for both, it gets confusing to maintain
function mockTopicGetRequest({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?:
    | "error"
    | "empty"
    | "multiple-pages-static"
    | "single-page-static"
    | "single-page-env-dev";
}) {
  mswInstance.use(
    rest.get("getTopics", async (req, res, ctx) => {
      const currentPage = req.url.searchParams.get("pageNo");
      const env = req.url.searchParams.get("env");

      // error path
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }
      // response empty
      else if (scenario === "empty") {
        return res(ctx.status(200), ctx.json([]));

        // response total pages 4, current page based on api
      } else if (scenario === "multiple-pages-static") {
        return res(ctx.status(200), ctx.json(mockedResponseMultiplePage));
      } else if (scenario === "single-page-static") {
        return res(ctx.status(200), ctx.json(mockedResponseSinglePage));
      } else if (scenario === "single-page-env-dev") {
        return res(ctx.status(200), ctx.json(mockedResponseTopicEnv));
      }

      if (env === "DEV") {
        return res(ctx.status(200), ctx.json(mockedResponseTopicEnv));
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

const mockedResponseMultiplePageTransformed = transformTopicApiResponse(
  mockedResponseMultiplePage
);

const mockedResponseTopicEnv = [
  [
    createMockTopic({
      topicName: "Topic 1",
      topicId: 1,
      environmentsList: [TopicEnv.DEV],
    }),
    createMockTopic({
      topicName: "Topic 2",
      topicId: 2,
      environmentsList: [TopicEnv.DEV],
    }),
    createMockTopic({
      topicName: "Topic 3",
      topicId: 3,
      environmentsList: [TopicEnv.DEV],
    }),
  ],
];

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(
  mockedResponseSinglePage
);

function mockGetEnvs({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error";
}) {
  mswInstance.use(
    rest.get("getEnvs", async (req, res, ctx) => {
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }

      return res(
        ctx.status(200),
        ctx.json([
          createMockTopicEnvDTO(TopicEnv.DEV),
          createMockTopicEnvDTO(TopicEnv.TST),
        ])
      );
    })
  );
}

function mockGetTeams({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error";
}) {
  mswInstance.use(
    rest.get("getAllTeamsSUOnly", async (req, res, ctx) => {
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }

      return res(
        ctx.status(200),
        ctx.json(["TEST_TEAM_01", "TEST_TEAM_02", "TEST_TEAM_03"])
      );
    })
  );
}

export {
  mockGetEnvs,
  mockTopicGetRequest,
  mockGetTeams,
  mockedResponseTransformed,
  mockedResponseMultiplePageTransformed,
};
