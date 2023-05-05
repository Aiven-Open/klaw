import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import TopicFilter from "src/app/features/components/filters/TopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import TopicTable from "src/app/features/topics/browse/components/TopicTable";
import { getTopics } from "src/domain/topic";

function BrowseTopics() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { topic, environment, teamId } = useFiltersValues();

  const {
    data: topics,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["browseTopics", currentPage, topic, environment, teamId],
    queryFn: () =>
      getTopics({
        pageNo: currentPage.toString(),
        env: environment,
        teamId: teamId === "ALL" ? undefined : Number(teamId),
        topicnamesearch: topic.length === 0 ? undefined : topic,
      }),
    keepPreviousData: true,
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
        <EnvironmentFilter key="environment" />,
        <TopicFilter key="search" />,
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

export default BrowseTopics;
