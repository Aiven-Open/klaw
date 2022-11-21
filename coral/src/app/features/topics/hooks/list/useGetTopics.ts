import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import { mockTopicGetRequest } from "src/domain/topic/topic-api.msw";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { Environment } from "src/domain/environment";
import { topicsQuery } from "src/domain/topic/topic-queries";

function useGetTopics({
  currentPage,
  environment,
  teamName,
}: {
  currentPage: number;
  environment: Environment;
  teamName?: string;
}): UseQueryResult<TopicApiResponse> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockTopicGetRequest({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<TopicApiResponse, Error>(
    topicsQuery({ currentPage, environment, teamName })
  );
}

export { useGetTopics };
