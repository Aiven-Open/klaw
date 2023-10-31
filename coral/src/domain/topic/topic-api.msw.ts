import { rest } from "msw";
import { SetupServer } from "msw/node";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { createMockTopicApiResponse } from "src/domain/topic/topic-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";
import { KlawApiResponse, KlawApiModel } from "types/utils";
import { TopicTeam, TopicNames } from "src/domain/topic";

const mockedResponseSinglePage: KlawApiResponse<"getTopics"> =
  createMockTopicApiResponse({
    entries: 10,
  });

const mockedResponseMultiplePage: KlawApiResponse<"getTopics"> =
  createMockTopicApiResponse({
    entries: 2,
    totalPages: 4,
    currentPage: 2,
  });

const mockedResponseMultiplePageTransformed = transformTopicApiResponse(
  mockedResponseMultiplePage
);

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(
  mockedResponseSinglePage
);

interface MockGetTopicNamesRequestArgs {
  mswInstance: SetupServer;
  response: TopicNames;
}

function mockGetTopicNames({
  mswInstance,
  response,
}: MockGetTopicNamesRequestArgs) {
  const base = getHTTPBaseAPIUrl();

  const handler = rest.get(`${base}/getTopicsOnly`, (_, res, ctx) => {
    return res(ctx.status(200), ctx.json(response));
  });

  mswInstance.use(handler);
}

const mockedResponseTopicNames: KlawApiResponse<"getTopicsOnly"> = [
  "aivtopic1",
  "topic-two",
  "topic-myteam",
];

interface MockGetTopicTeamRequestArgs {
  mswInstance: SetupServer;
  response: TopicTeam;
  topicName: string;
  patternType?: "LITERAL" | "PREFIXED";
}

function mockGetTopicTeam({
  mswInstance,
  response,
}: MockGetTopicTeamRequestArgs) {
  const base = getHTTPBaseAPIUrl();

  const handler = rest.get(`${base}/getTopicTeam`, (_, res, ctx) => {
    return res(ctx.status(200), ctx.json(response));
  });

  mswInstance.use(handler);
}

const mockedResponseTopicTeamLiteral: KlawApiModel<"TopicTeamResponse"> = {
  status: true,
  team: "Ospo",
  teamId: 1,
};

export {
  mockedResponseTransformed,
  mockedResponseMultiplePageTransformed,
  mockGetTopicNames,
  mockedResponseTopicNames,
  mockGetTopicTeam,
  mockedResponseTopicTeamLiteral,
};
