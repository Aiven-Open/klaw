import { getTopics } from "src/domain/topic/topic-api";

export const topicsQuery = ({
  currentPage,
  environment,
  teamName,
  searchTerm,
}: {
  currentPage: number;
  environment: string;
  teamName?: string;
  searchTerm?: string;
}) => {
  return {
    queryKey: ["topics", currentPage, environment, teamName, searchTerm],
    queryFn: () =>
      getTopics({ currentPage, environment, teamName, searchTerm }),
    keepPreviousData: true,
  };
};
