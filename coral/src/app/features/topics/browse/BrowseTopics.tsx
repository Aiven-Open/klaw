import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import TopicTable from "src/app/features/topics/browse/components/TopicTable";
import { getTopics } from "src/domain/topic";
import { TopicTypeFilter } from "src/app/features/components/filters/TopicTypeFilter";

function BrowseTopics() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, teamId, topicType } = useFiltersContext();

  const {
    data: topics,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: [
      "browseTopics",
      currentPage,
      search,
      environment,
      teamId,
      topicType,
    ],
    queryFn: () =>
      getTopics({
        pageNo: currentPage.toString(),
        env: environment,
        teamId: teamId === "ALL" ? undefined : Number(teamId),
        topicnamesearch: search.length === 0 ? undefined : search,
        topicType: topicType === "ALL" ? undefined : topicType,
      }),
    keepPreviousData: true,
    refetchOnWindowFocus: true,
  });

  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }
  const pagination =
    topics && topics.totalPages > 1 ? (
      <Pagination
        activePage={topics.currentPage}
        totalPages={topics.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[
        <TeamFilter key="team" />,
        <EnvironmentFilter
          key="environment"
          environmentsFor={"TOPIC_AND_ACL"}
        />,
        <TopicTypeFilter key={"topicType"} />,
        <SearchTopicFilter key={"search"} />,
      ]}
      table={
        <TopicTable
          topics={topics?.entries ?? []}
          ariaLabel={`Topics overview, page ${topics?.currentPage ?? 0} of ${
            topics?.totalPages ?? 0
          }`}
        />
      }
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export default withFiltersContext({ element: <BrowseTopics /> });
