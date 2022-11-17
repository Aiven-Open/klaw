import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import { mockTopicGetRequest } from "src/domain/topics/topics-api.msw";
import { getTopics, TopicEnv } from "src/domain/topics";
import { TopicApiResponse } from "src/domain/topics/topics-types";

function useGetTopics({
  currentPage,
  topicEnv,
}: {
  currentPage: number;
  topicEnv: TopicEnv;
}): UseQueryResult<TopicApiResponse> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockTopicGetRequest({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<TopicApiResponse, Error>({
    queryKey: ["topics", currentPage, topicEnv],
    queryFn: () => getTopics({ currentPage, topicEnv }),
    keepPreviousData: true,
  });
}

export { useGetTopics };
