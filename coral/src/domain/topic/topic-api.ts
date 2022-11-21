import {
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topic/topic-types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { Environment } from "src/domain/environment";

const getTopics = async ({
  currentPage = 1,
  environment = "ALL",
  teamName,
}: {
  currentPage: number;
  environment: Environment;
  teamName?: string;
}): Promise<TopicApiResponse> => {
  const team = teamName && teamName !== "All teams" ? teamName : null;
  const params: Record<string, string> = {
    pageNo: currentPage.toString(),
    env: environment,
    ...(team && { teamName: team }),
  };

  return fetch(`/getTopics?${new URLSearchParams(params)}`, {
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
