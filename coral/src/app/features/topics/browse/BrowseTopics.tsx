import { useGetTopics } from "src/app/features/topics/browse/hooks/topic-list/useGetTopics";
import SelectTeam from "src/app/features/topics/browse/components/select-team/SelectTeam";
import TopicTable from "src/app/features/topics/browse/components/topic-table/TopicTable";
import { useState } from "react";
import { Box } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import SelectEnvironment from "src/app/features/topics/browse/components/select-environment/SelectEnvironment";
import { SearchTopics } from "src/app/features/topics/browse/components/search/SearchTopics";
import { Team, TEAM_NOT_INITIALIZED } from "src/domain/team";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";
import { Pagination } from "src/app/features/components/Pagination";

function BrowseTopics() {
  const [page, setPage] = useState(1);
  const [searchParams, setSearchParams] = useSearchParams();
  const [teamName, setTeamName] = useState<Team>(TEAM_NOT_INITIALIZED);
  const [environment, setEnvironment] = useState(ENVIRONMENT_NOT_INITIALIZED);

  const initialSearchTerm = searchParams.get("search");
  const [searchTerm, setSearchTerm] = useState<string>(initialSearchTerm || "");

  const {
    data: topics,
    isLoading,
    isError,
    isPreviousData,
  } = useGetTopics({
    currentPage: page,
    environment,
    teamName,
    ...(searchTerm && { searchTerm: searchTerm }),
  });

  const hasTopics = topics && topics.entries.length > 0;

  return (
    <>
      <Box display={"flex"} flexDirection={"column"} gap={"l1"}>
        <Box
          display={"flex"}
          justifyContent={"end"}
          colGap={"l1"}
          maxWidth={"7xl"}
        >
          <SearchTopics onChange={searchTopics} value={searchTerm} />
        </Box>
        <Box
          display={"flex"}
          flexDirection={"row"}
          colGap={"l1"}
          maxWidth={"7xl"}
        >
          <Box grow={1}>
            <SelectTeam onChange={setTeamName} />
          </Box>
          <Box grow={1}>
            <SelectEnvironment onChange={setEnvironment} />
          </Box>
        </Box>
        <Box
          display={"flex"}
          flexDirection={"column"}
          alignItems={"center"}
          rowGap={"l4"}
        >
          {isPreviousData && <div>Filtering list...</div>}
          {isLoading && <div>Loading...</div>}
          {isError && <div>Something went wrong 😔</div>}

          {!isLoading && !hasTopics && <div>No topics found</div>}
          {hasTopics && (
            <>
              <TopicTable
                topics={topics.entries}
                activePage={page}
                totalPages={topics.totalPages}
              />
              <Pagination
                totalPages={topics?.totalPages}
                initialPage={page}
                setPage={setPage}
              />
            </>
          )}
        </Box>
      </Box>
    </>
  );

  function searchTopics(newSearchTerm: string) {
    setSearchTerm(newSearchTerm);
    if (newSearchTerm.length === 0) {
      searchParams.delete("search");
    } else {
      searchParams.set("search", newSearchTerm);
    }

    setSearchParams(searchParams);
  }
}

export default BrowseTopics;
