import { getTopics } from "src/domain/topic/topic-api";

export const topicsQuery = ({
  currentPage,
  environment,
  teamName,
}: {
  currentPage: number;
  environment: string;
  teamName?: string;
}) => {
  return {
    queryKey: ["topics", currentPage, environment, teamName],
    queryFn: () => getTopics({ currentPage, environment, teamName }),
    keepPreviousData: true,
  };
};
