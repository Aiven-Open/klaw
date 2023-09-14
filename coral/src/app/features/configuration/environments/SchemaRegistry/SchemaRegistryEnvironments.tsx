import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { SearchFilter } from "src/app/features/components/filters/SearchFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import SchemaRegistryEnvironmentsTable from "src/app/features/configuration/environments/SchemaRegistry/components/SchemaRegistryEnvironmentsTable";
import getPaginatedEnvironments from "src/app/features/configuration/environments/hooks/getPaginatedEnvironments";

const SchemaRegistryEnvironments = () => {
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
    type: "schemaregistry",
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
    <SchemaRegistryEnvironmentsTable
      environments={environments?.entries ?? []}
      ariaLabel={`Schema Registry Environments overview, page ${
        environments?.currentPage ?? 0
      } of ${environments?.totalPages ?? 0}`}
    />
  );

  return (
    <TableLayout
      filters={[
        <SearchFilter
          key="search"
          placeholder={"Search Schema Registry Environment"}
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
  element: <SchemaRegistryEnvironments />,
});
