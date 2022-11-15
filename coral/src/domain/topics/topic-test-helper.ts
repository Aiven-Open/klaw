import { Topic, TopicDTOApiResponse } from "src/domain/topics/topics-types";

function createRandomTopicId() {
  return Math.floor(Math.random() * 9000 + 1000);
}

function createMockTopic({
  topicName,
  topicId = createRandomTopicId(),
}: {
  topicName?: string;
  topicId?: number;
}): Topic {
  const name = topicName ? topicName : "Mock topic " + createRandomTopicId();

  return {
    topicid: topicId,
    sequence: "341",
    totalNoPages: "1",
    currentPage: "1",
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
  entries: number,
}: {
  entries: 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10;
}): TopicDTOApiResponse {
  const response: TopicDTOApiResponse = [[]];
  if (number >= 4 && number <= 6) {
    response.push([]);
  }
  if (number >= 7 && number <= 9) {
    response.push([]);
    response.push([]);
  }
  if (number === 10) {
    response.push([]);
    response.push([]);
    response.push([]);
  }

  while (number > +0) {
    let subArray = 0;
    if (number >= 4 && number <= 6) {
      subArray = 1;
    }
    if (number >= 7 && number <= 9) {
      subArray = 2;
    }
    if (number === 10) {
      subArray = 3;
    }
    number--;
    response[subArray].push(createMockTopic({}));
  }

  return response;
}

export { createMockTopic, createMockTopicApiResponse };
