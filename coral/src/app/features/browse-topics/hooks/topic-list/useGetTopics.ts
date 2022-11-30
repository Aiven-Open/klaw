import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { Environment } from "src/domain/environment";
import { topicsQuery } from "src/domain/topic/topic-queries";

function useGetTopics({
  currentPage,
  environment,
  teamName,
  searchTerm,
}: {
  currentPage: number;
  environment: Environment;
  teamName: string;
  searchTerm?: string;
}): UseQueryResult<TopicApiResponse> {
  return useQuery<TopicApiResponse, Error>(
    topicsQuery({ currentPage, environment, teamName, searchTerm })
  );
}

export { useGetTopics };
