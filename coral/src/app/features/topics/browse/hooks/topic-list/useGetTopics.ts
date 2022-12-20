import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { topicsQuery } from "src/domain/topic/topic-queries";
import { Team } from "src/domain/team";

function useGetTopics({
  currentPage,
  environment,
  teamName,
  searchTerm,
}: {
  currentPage: number;
  environment: string;
  teamName: Team;
  searchTerm?: string;
}): UseQueryResult<TopicApiResponse> {
  return useQuery<TopicApiResponse, Error>(
    topicsQuery({ currentPage, environment, teamName, searchTerm })
  );
}

export { useGetTopics };
