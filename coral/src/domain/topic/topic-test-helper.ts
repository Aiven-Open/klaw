import { Topic, TopicRequest } from "src/domain/topic/topic-types";
import { KlawApiResponse } from "types/utils";

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
    envId: "1",
    envName: "DEV",
    environmentsList: expect.arrayContaining([expect.any(String)]),
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
  environmentsList = ["DEV", "TEST"],
}: {
  topicName: string;
  topicId: number;
  totalNoPages?: number;
  currentPage?: number;
  environmentsList?: string[];
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

const defaultTopicRequest: TopicRequest = {
  topicname: "test-topic-1",
  environment: "1",
  topicpartitions: 4,
  teamname: "NCC1701D",
  remarks: "asap",
  description: "This topic is for test",
  replicationfactor: "2",
  environmentName: "BRG",
  topicid: 1000,
  advancedTopicConfigEntries: [
    {
      configKey: "cleanup.policy",
      configValue: "delete",
    },
  ],
  requestOperationType: "CREATE",
  requestor: "jlpicard",
  requesttime: "1987-09-28T13:37:00.001+00:00",
  requesttimestring: "28-Sep-1987 13:37:00",
  requestStatus: "CREATED",
  totalNoPages: "1",
  approvingTeamDetails:
    "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
  teamId: 1003,
  allPageNos: ["1"],
  currentPage: "1",
  editable: true,
  deletable: true,
  deleteAssociatedSchema: false,
};

function createMockTopicRequest(request?: Partial<TopicRequest>): TopicRequest {
  return { ...defaultTopicRequest, ...request };
}

export {
  createMockTopic,
  createMockTopicApiResponse,
  baseTestObjectMockedTopic,
  createMockTopicRequest,
};
