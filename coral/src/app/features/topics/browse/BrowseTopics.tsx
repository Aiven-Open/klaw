import { TopicsTable } from "klaw";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";

function BrowseTopics() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, teamId } = useFiltersContext();

  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }
  return (
    <>
      <TeamFilter key="team" />
      <EnvironmentFilter key="environment" environmentsFor={"TOPIC_AND_ACL"} />

      <SearchTopicFilter key={"search"} />
      <TopicsTable
        ariaLabel="Hello"
        params={{
          pageNo: currentPage.toString(),
          env: environment,
          teamId: teamId === "ALL" ? undefined : Number(teamId),
          topicnamesearch: search.length === 0 ? undefined : search,
        }}
      />
      <Pagination
        activePage={currentPage}
        totalPages={1000}
        setActivePage={handleChangePage}
      />
    </>
  );
}

export default withFiltersContext({ element: <BrowseTopics /> });
