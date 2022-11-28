import { getTopics } from "src/domain/topic/topic-api";
import { Team } from "src/domain/team";

export const topicsQuery = ({
  currentPage,
  environment,
  teamName,
  searchTerm,
}: {
  currentPage: number;
  environment: string;
  // null represents the initial state
  // for teamName to avoid fetching
  // until the value from searchQuery is not
  // evaluated
  teamName: Team | null;
  searchTerm?: string;
}) => {
  return {
    queryKey: ["topics", currentPage, environment, teamName, searchTerm],
    queryFn: () =>
      getTopics({ currentPage, environment, teamName, searchTerm }),
    keepPreviousData: true,
    enabled: !!teamName,
  };
};
