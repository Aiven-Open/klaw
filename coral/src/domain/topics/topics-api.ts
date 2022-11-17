import {
  TopicApiResponse,
  TopicDTOApiResponse,
  TopicEnv,
} from "src/domain/topics/topics-types";
import {
  transformTopicApiResponse,
  transformTopicEnvApiResponse,
} from "src/domain/topics/topic-transformer";

const getTopics = async ({
  currentPage = 1,
  topicEnv = TopicEnv.ALL,
}: {
  currentPage: number;
  topicEnv: TopicEnv;
}): Promise<TopicApiResponse> => {
  return fetch(`/getTopics?env=${topicEnv}&pageNo=${currentPage}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`msw error: ${response.statusText}`);
      }
      const test: TopicDTOApiResponse = await response.json();
      return transformTopicApiResponse(test);
    })
    .catch((error) => {
      throw new Error(error);
    });
};

const getEnvs = async (): Promise<TopicEnv[]> => {
  return fetch(`/getEnvs`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`msw error: ${response.statusText}`);
      }
      const result = await response.json();
      return transformTopicEnvApiResponse(result);
    })
    .catch((error) => {
      throw new Error(error);
    });
};

export { getTopics, getEnvs };
