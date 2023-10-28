import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import KafkaConnectEnvironmentsTable from "src/app/features/configuration/environments/KafkaConnect/components/KafkaConnectEnvironmentsTable";
import getPaginatedEnvironments from "src/app/features/configuration/environments/hooks/getPaginatedEnvironments";
import {LoadingTableColumn} from 'src/app/features/components/layouts/LoadingTable';

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

  const { environments, isLoading, isError, error } = getPaginatedEnvironments({
    type: "kafkaconnect",
    currentPage,
    search,
  });

  const pagination =
    environments && environments.totalPages > 1 ? (
      <Pagination
        activePage={environments.currentPage}
        totalPages={environments.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const table = (
    <KafkaConnectEnvironmentsTable
      environments={environments?.entries ?? []}
      ariaLabel={`Kafka Connect Environments overview, page ${
        environments?.currentPage ?? 0
      } of ${environments?.totalPages ?? 0}`}
    />
  );

    // Calculate rowLength
const rowLength = environments?.entries.length || 0;

// Calculate columns
let columns: LoadingTableColumn[] = [];
if(environments && environments.entries && environments.entries.length > 0){
  columns = Object.keys(environments.entries[0]).map((key) => ({
            headerName: key,
       }));
}

  return (
    <TableLayout
      filters={[
        <SearchFilter
          key="search"
          label={"Search Kafka Connect Environment"}
          placeholder={"DEV, TST, ..."}
          description={
            "A partial match for an environment name, a cluster name, a tenant name."
          }
          ariaDescription={`Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.`}
        />,
      ]}
      table={table}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
      loadingState={{
        rowLength: rowLength,
        columns: columns,
      }}
    />
  );
};

export default withFiltersContext({
  element: <KafkaConnectEnvironments />,
});
