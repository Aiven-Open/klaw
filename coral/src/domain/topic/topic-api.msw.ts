import { rest } from "msw";
import { SetupServer } from "msw/node";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { createMockTopicApiResponse } from "src/domain/topic/topic-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";
import { KlawApiResponse, KlawApiModel } from "types/utils";
import { TopicTeam, TopicNames } from "src/domain/topic";

function mockgetTopicAdvancedConfigOptions({
  mswInstance,
  response,
}: {
  mswInstance: SetupServer;
  response: {
    status?: number;
    data: KlawApiResponse<"getAdvancedTopicConfigs"> | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/getAdvancedTopicConfigs`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

function mockRequestTopic({
  mswInstance,
  response,
}: {
  mswInstance: SetupServer;
  response: {
    status?: number;
    data: KlawApiResponse<"createTopicsCreateRequest"> | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/createTopics`;
  mswInstance.use(
    rest.post(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

const defaultgetTopicAdvancedConfigOptionsResponse = {
  CLEANUP_POLICY: "cleanup.policy",
  COMPRESSION_TYPE: "compression.type",
  DELETE_RETENTION_MS: "delete.retention.ms",
  FILE_DELETE_DELAY_MS: "file.delete.delay.ms",
  FLUSH_MESSAGES: "flush.messages",
  FLUSH_MS: "flush.ms",
  FOLLOWER_REPLICATION_THROTTLED_REPLICAS:
    "follower.replication.throttled.replicas",
  INDEX_INTERVAL_BYTES: "index.interval.bytes",
  LEADER_REPLICATION_THROTTLED_REPLICAS:
    "leader.replication.throttled.replicas",
  MAX_COMPACTION_LAG_MS: "max.compaction.lag.ms",
  MAX_MESSAGE_BYTES: "max.message.bytes",
  MESSAGE_DOWNCONVERSION_ENABLE: "message.downconversion.enable",
  MESSAGE_FORMAT_VERSION: "message.format.version",
  MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS: "message.timestamp.difference.max.ms",
  MESSAGE_TIMESTAMP_TYPE: "message.timestamp.type",
  MIN_CLEANABLE_DIRTY_RATIO: "min.cleanable.dirty.ratio",
  MIN_COMPACTION_LAG_MS: "min.compaction.lag.ms",
  MIN_INSYNC_REPLICAS: "min.insync.replicas",
  PREALLOCATE: "preallocate",
  RETENTION_BYTES: "retention.bytes",
  RETENTION_MS: "retention.ms",
  SEGMENT_BYTES: "segment.bytes",
  SEGMENT_INDEX_BYTES: "segment.index.bytes",
  SEGMENT_JITTER_MS: "segment.jitter.ms",
  SEGMENT_MS: "segment.ms",
  UNCLEAN_LEADER_ELECTION_ENABLE: "unclean.leader.election.enable",
};

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

function mockGetTopicRequestsForApprover({
  mswInstance,
  response,
}: {
  mswInstance: SetupServer;
  response: {
    status?: number;
    data: KlawApiResponse<"getTopicRequestsForApprover"> | { message: string };
  };
}) {
  mswInstance.use(
    rest.get(
      `${getHTTPBaseAPIUrl()}/getTopicRequestsForApprover`,
      (_, res, ctx) =>
        res(ctx.status(response.status ?? 200), ctx.json(response.data))
    )
  );
}

export {
  mockgetTopicAdvancedConfigOptions,
  mockRequestTopic,
  mockedResponseTransformed,
  mockedResponseMultiplePageTransformed,
  mockedResponseSinglePage,
  mockedResponseMultiplePage,
  defaultgetTopicAdvancedConfigOptionsResponse,
  mockGetTopicNames,
  mockedResponseTopicNames,
  mockGetTopicTeam,
  mockedResponseTopicTeamLiteral,
  mockGetTopicRequestsForApprover,
};
