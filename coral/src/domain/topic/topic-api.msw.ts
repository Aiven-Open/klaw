import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import {
  createMockTopic,
  createMockTopicApiResponse,
} from "src/domain/topic/topic-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";
import { KlawApiResponse } from "types/utils";
import { TopicTeam, TopicNames } from "src/domain/topic";

type MockedResponse = {
  status?: number;
  data: KlawApiResponse<"topicsGet"> | { message: string };
};

type MockTopicGetRequestArgs =
  | {
      mswInstance: MswInstance;
      response: MockedResponse;
    }
  | {
      mswInstance: MswInstance;
      responses: MockedResponse[];
    };

function mockTopicGetRequest({
  mswInstance,
  ...responseOrResponses
}: MockTopicGetRequestArgs) {
  const base = getHTTPBaseAPIUrl();
  const responses =
    "responses" in responseOrResponses
      ? responseOrResponses.responses
      : [responseOrResponses.response];

  const handlers = responses.map((response) => {
    return rest.get(`${base}/getTopics`, (_, res, ctx) => {
      return res.once(
        ctx.status(response.status ?? 200),
        ctx.json(response.data)
      );
    });
  });

  mswInstance.use(...handlers);
}

function mockgetTopicAdvancedConfigOptions({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<"topicAdvancedConfigGet"> | { message: string };
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
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<"topicCreate"> | { message: string };
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

// This mirrors the formatting formation used in the api call
// for usage in tests that use the mock API
const mockedResponseTransformed = transformTopicApiResponse(
  mockedResponseSinglePage
);

interface MockGetTopicNamesRequestArgs {
  mswInstance: MswInstance;
  response: TopicNames;
  isMyTeamTopics?: boolean;
}

function mockGetTopicNames({
  mswInstance,
  response,
  isMyTeamTopics = false,
}: MockGetTopicNamesRequestArgs) {
  const base = getHTTPBaseAPIUrl();
  const params = new URLSearchParams({
    isMyTeamTopics: isMyTeamTopics.toString(),
  });

  const handler = rest.get(`${base}/getTopicsOnly?${params}`, (_, res, ctx) => {
    return res.once(ctx.status(200), ctx.json(response));
  });

  mswInstance.use(handler);
}

const mockedResponseTopicNames: KlawApiResponse<"topicsGetOnly"> = [
  "aivtopic1",
  "topic-two",
  "topic-myteam",
];

const mockedResponseTopicNamesMyTeamOnly: KlawApiResponse<"topicsGetOnly"> = [
  "topic-myteam",
];

interface MockGetTopicTeamRequestArgs {
  mswInstance: MswInstance;
  response: TopicTeam;
  topicName: string;
  patternType?: "LITERAL" | "PREFIXED";
}

function mockGetTopicTeam({
  mswInstance,
  response,
  topicName,
  patternType = "LITERAL",
}: MockGetTopicTeamRequestArgs) {
  const base = getHTTPBaseAPIUrl();
  const params = new URLSearchParams({ topicName, patternType });

  const handler = rest.get(`${base}/getTopicTeam?${params}`, (_, res, ctx) => {
    return res.once(ctx.status(200), ctx.json(response));
  });

  mswInstance.use(handler);
}

const mockedResponseTopicTeamLiteral: KlawApiResponse<"topicGetTeam"> = {
  team: "Ospo",
};
const mockedResponseTopicTeamPrefixed: KlawApiResponse<"topicGetTeam"> = {
  team: "prefixed-Ospo",
};

export {
  mockTopicGetRequest,
  mockgetTopicAdvancedConfigOptions,
  mockRequestTopic,
  mockedResponseTransformed,
  mockedResponseMultiplePageTransformed,
  mockedResponseSinglePage,
  mockedResponseMultiplePage,
  mockedResponseTopicEnv,
  defaultgetTopicAdvancedConfigOptionsResponse,
  mockGetTopicNames,
  mockedResponseTopicNames,
  mockedResponseTopicNamesMyTeamOnly,
  mockGetTopicTeam,
  mockedResponseTopicTeamLiteral,
  mockedResponseTopicTeamPrefixed,
};
