import { rest } from "msw";
import { SetupServer } from "msw/node";
import { getHTTPBaseAPIUrl } from "src/config";
import { TopicNames, TopicTeam } from "src/domain/topic";

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

export { mockGetTopicNames, mockGetTopicTeam };
