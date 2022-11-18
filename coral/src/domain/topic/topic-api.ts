import {
  TopicApiResponse,
  TopicDTOApiResponse,
  TopicEnv,
  TopicTeams,
} from "src/domain/topic/topic-types";
import {
  transformTopicApiResponse,
  transformTopicEnvApiResponse,
} from "src/domain/topic/topic-transformer";

const getTopics = async ({
  currentPage = 1,
  topicEnv = TopicEnv.ALL,
  teamName,
}: {
  currentPage: number;
  topicEnv: TopicEnv;
  teamName?: string;
}): Promise<TopicApiResponse> => {
  const team = teamName && teamName !== "All teams" ? teamName : null;
  const params: Record<string, string> = {
    pageNo: currentPage.toString(),
    env: topicEnv,
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

const getTeams = async (): Promise<TopicTeams> => {
  return fetch(`/getAllTeamsSUOnly`, {
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
      return result;
    })
    .catch((error) => {
      throw new Error(error);
    });
};

export { getTopics, getEnvs, getTeams };
