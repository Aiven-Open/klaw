import { EnvironmentInfo } from "src/domain/environment/environment-types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { Topic } from "src/domain/topic/topic-types";
import { KlawApiModel, KlawApiResponse } from "types/utils";

// currently this file is used in code (topcis-api.msw.ts)
// so "expect" is not defined there
const baseTestObjectMockedTopic = (): Topic => {
  return {
    topicid: expect.any(Number),
    totalNoPages: expect.any(String),
    currentPage: expect.any(String),
    topicName: expect.any(String),
    noOfPartitions: 2,
    sequence: "341",
    allPageNos: ["1"],
    description: "Topic description",
    noOfReplicas: "2",
    teamname: "DevRel",
    teamId: 2,
    envId: "1",
    envName: "DEV",
    environmentsList: expect.arrayContaining([expect.any(Object)]),
    showEditTopic: false,
    showDeleteTopic: false,
    topicDeletable: false,
  };
};

function createMockTopic({
  topicName,
  topicId,
  totalNoPages = 1,
  currentPage = 1,
  environmentsList = [
    { id: "1", name: "DEV" },
    { id: "2", name: "TEST" },
  ],
}: {
  topicName: string;
  topicId: number;
  totalNoPages?: number;
  currentPage?: number;
  environmentsList?: EnvironmentInfo[];
}): Topic {
  return {
    topicid: topicId,
    sequence: "341",
    totalNoPages: `${totalNoPages}`,
    currentPage: `${currentPage}`,
    allPageNos: ["1"],
    topicName: topicName,
    noOfPartitions: 2,
    description: "Topic description",
    noOfReplicas: "2",
    teamname: "DevRel",
    teamId: 2,
    envId: "1",
    envName: "DEV",
    environmentsList,
    showEditTopic: false,
    showDeleteTopic: false,
    topicDeletable: false,
  };
}

function createMockTopicApiResponse({
  entries,
  totalPages = 1,
  currentPage = 1,
}: {
  entries: 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10;
  totalPages?: number;
  currentPage?: number;
}): KlawApiResponse<"getTopics"> {
  const response: KlawApiResponse<"getTopics"> = [[]];

  const totalPageNumber = currentPage > totalPages ? currentPage : totalPages;
  if (entries >= 4 && entries <= 6) {
    response.push([]);
  }
  if (entries >= 7 && entries <= 9) {
    response.push([]);
    response.push([]);
  }
  if (entries === 10) {
    response.push([]);
    response.push([]);
    response.push([]);
  }

  let topicId = 0;
  while (entries > 0) {
    let subArray = 0;
    if (entries >= 4 && entries <= 6) {
      subArray = 1;
    }
    if (entries >= 7 && entries <= 9) {
      subArray = 2;
    }
    if (entries === 10) {
      subArray = 3;
    }

    response[subArray].push(
      createMockTopic({
        topicName: `Mocked topic nr ${topicId} page ${currentPage}`,
        topicId: topicId,
        totalNoPages: totalPageNumber,
        currentPage: currentPage,
      })
    );
    entries--;
    topicId++;
  }

  return response;
}

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

const mockedResponseTopicNames: KlawApiResponse<"getTopicsOnly"> = [
  "aivtopic1",
  "topic-two",
  "topic-myteam",
];

const mockedResponseTopicTeamLiteral: KlawApiModel<"TopicTeamResponse"> = {
  status: true,
  team: "Ospo",
  teamId: 1,
};

export {
  baseTestObjectMockedTopic,
  createMockTopic,
  createMockTopicApiResponse,
  mockedResponseMultiplePageTransformed,
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
  mockedResponseTransformed,
};
