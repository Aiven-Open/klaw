import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import KafkaConnectEnvironmentsTable from "src/app/features/configuration/environments/KafkaConnect/components/KafkaConnectEnvironmentsTable";
import { getPaginatedEnvironmentsForConnector } from "src/domain/environment";

const KafkaConnectEnvironments = () => {
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
    data: kafkaConnectEnvs,
    isLoading,
    isError,
    error,
  } = useQuery(["getPaginatedEnvironmentsForConnector", currentPage, search], {
    queryFn: () =>
      getPaginatedEnvironmentsForConnector({
        pageNo: String(currentPage),
        searchEnvParam: search.length === 0 ? undefined : search,
      }),
  });

  const pagination =
    kafkaConnectEnvs && kafkaConnectEnvs.totalPages > 1 ? (
      <Pagination
        activePage={kafkaConnectEnvs.currentPage}
        totalPages={kafkaConnectEnvs.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const table = (
    <KafkaConnectEnvironmentsTable
      environments={kafkaConnectEnvs?.entries ?? []}
      ariaLabel={`Kafka Connect Environments overview, page ${
        kafkaConnectEnvs?.currentPage ?? 0
      } of ${kafkaConnectEnvs?.totalPages ?? 0}`}
    />
  );

  return (
    <TableLayout
      filters={[
        <SearchFilter
          key="search"
          placeholder={"Search Kafka Connect Environment"}
          description={`Search for a partial match for an environment name, a cluster name, a tenant name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
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
  element: <KafkaConnectEnvironments />,
});
