import {
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topics/topics-types";
import { transformTopicApiResponse } from "src/domain/topics/topic-transformer";

const getTopics = async (currentPage: number): Promise<TopicApiResponse> => {
  return fetch(`/getTopics?pageNo=${currentPage}`, {
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

export { getTopics };
