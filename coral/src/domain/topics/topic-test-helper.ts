import { Topic, TopicDTOApiResponse } from "src/domain/topics/topics-types";

function createRandomTopicId() {
  return Math.floor(Math.random() * 9000 + 1000);
}

function createMockTopic({
  topicName,
  topicId = createRandomTopicId(),
  totalNoPages = 1,
  currentPage = 1,
}: {
  topicName?: string;
  topicId?: number;
  totalNoPages?: number;
  currentPage?: number;
}): Topic {
  const name = topicName ? topicName : "Mock topic " + createRandomTopicId();

  return {
    topicid: topicId,
    sequence: "341",
    totalNoPages: `${totalNoPages}`,
    currentPage: `${currentPage}`,
    allPageNos: ["1"],
    topicName: name,
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

  while (entries > +0) {
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
    entries--;
    response[subArray].push(
      createMockTopic({
        totalNoPages: totalPageNumber,
        currentPage: currentPage,
      })
    );
  }

  return response;
}

export { createMockTopic, createMockTopicApiResponse };
