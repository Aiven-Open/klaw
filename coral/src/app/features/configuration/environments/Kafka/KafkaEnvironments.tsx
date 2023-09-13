import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import KafkaEnvironmentsTable from "src/app/features/configuration/environments/Kafka/components/KafkaEnvironmentsTable";
import { getPaginatedEnvironmentsForTopicAndAcl } from "src/domain/environment";

const KafkaEnvironments = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { search } = useFiltersContext();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }
  const {
    data: kafkaEnvs,
    isLoading,
    isError,
    error,
  } = useQuery(
    ["getPaginatedEnvironmentsForTopicAndAcl", currentPage, search],
    {
      queryFn: () =>
        getPaginatedEnvironmentsForTopicAndAcl({
          pageNo: String(currentPage),
          searchEnvParam: search.length === 0 ? undefined : search,
        }),
    }
  );

  const pagination =
    kafkaEnvs && kafkaEnvs.totalPages > 1 ? (
      <Pagination
        activePage={kafkaEnvs.currentPage}
        totalPages={kafkaEnvs.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const table = (
    <KafkaEnvironmentsTable
      environments={kafkaEnvs?.entries ?? []}
      ariaLabel={`Kafka Environments overview, page ${
        kafkaEnvs?.currentPage ?? 0
      } of ${kafkaEnvs?.totalPages ?? 0}`}
    />
  );

  return (
    <TableLayout
      filters={[
        <SearchFilter
          key="search"
          placeholder={"Search Kafka Environment"}
          description={`Search for a partial match for an environment name, a cluster name, a tenant name, a partition or replication parameter. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
        />,
      ]}
      table={table}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
};

export default withFiltersContext({
  element: <KafkaEnvironments />,
});
