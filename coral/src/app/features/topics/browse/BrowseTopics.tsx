import { useGetTopics } from "src/app/features/topics/browse/hooks/topic-list/useGetTopics";
import { Pagination } from "src/app/components/Pagination";
import SelectTeam from "src/app/features/topics/browse/components/select-team/SelectTeam";
import TopicTable from "src/app/features/topics/browse/components/topic-table/TopicTable";
import { useState } from "react";
import { Box, FlexboxItem } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import SelectEnvironment from "src/app/features/topics/browse/components/select-environment/SelectEnvironment";
import { SearchTopics } from "src/app/features/topics/browse/components/search/SearchTopics";
import { Team, TEAM_NOT_INITIALIZED } from "src/domain/team";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";

function BrowseTopics() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [teamName, setTeamName] = useState<Team>(TEAM_NOT_INITIALIZED);
  const [environment, setEnvironment] = useState(ENVIRONMENT_NOT_INITIALIZED);

  const initialPage = searchParams.get("page");
  const initialSearchTerm = searchParams.get("search");
  const [page, setPage] = useState(Number(initialPage) || 1);
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
  const hasMultiplePages = topics && topics.totalPages > 1;

  return (
    <>
      <Box display={"flex"} flexDirection={"column"} gap={"l1"}>
        <Box
          display={"flex"}
          justifyContent={"end"}
          colGap={"l1"}
          maxWidth={"6xl"}
        >
          <SearchTopics onChange={searchTopics} value={searchTerm} />
        </Box>
        <Box
          display={"flex"}
          flexDirection={"row"}
          colGap={"l1"}
          maxWidth={"6xl"}
        >
          {/*@TODO replace when Box has a grow prop*/}
          <FlexboxItem grow={1}>
            <SelectTeam onChange={setTeamName} />
          </FlexboxItem>
          {/*@TODO replace when Box has a grow prop*/}
          <FlexboxItem grow={1}>
            <SelectEnvironment onChange={setEnvironment} />
          </FlexboxItem>
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
            <TopicTable
              topics={topics.entries}
              activePage={topics.currentPage}
              totalPages={topics.totalPages}
            />
          )}

          {hasMultiplePages && (
            <Pagination
              activePage={topics.currentPage}
              totalPages={topics.totalPages}
              setActivePage={changePage}
            />
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

  function changePage(activePage: number) {
    setPage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  }
}

export default BrowseTopics;
