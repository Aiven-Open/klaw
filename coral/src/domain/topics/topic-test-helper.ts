import { Topic, TopicDTOApiResponse } from "src/domain/topics/topics-types";

// currently this file is used in code (topcis-api.msw.ts)
// so "expect" is not defined there
const baseTestObjectMockedTopic = () => {
  return {
    topicid: expect.any(Number),
    totalNoPages: expect.any(String),
    currentPage: expect.any(String),
    topicName: expect.any(String),
    noOfPartitions: 2,
    sequence: "341",
    allPageNos: ["1"],
    description: "Topic description",
    documentation: null,
    noOfReplcias: "2",
    teamname: "DevRel",
    cluster: "1",
    clusterId: null,
    environmentsList: ["DEV"],
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
}: {
  topicName: string;
  topicId: number;
  totalNoPages?: number;
  currentPage?: number;
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
    documentation: null,
    noOfReplcias: "2",
    teamname: "DevRel",
    cluster: "1",
    clusterId: null,
    environmentsList: ["DEV"],
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
}): TopicDTOApiResponse {
  const response: TopicDTOApiResponse = [[]];

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

export {
  createMockTopic,
  createMockTopicApiResponse,
  baseTestObjectMockedTopic,
};
