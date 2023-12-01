import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getClustersPaginated } from "src/domain/cluster";
import { ClustersTable } from "src/app/features/configuration/clusters/components/ClustersTable";

function Clusters() {
  const {
    data: clusters,
    isLoading,
    isError,
    error,
  } = useQuery(["get-clusters-paginated"], {
    queryFn: () => getClustersPaginated({ pageNo: "1" }),
  });

  return (
    <TableLayout
      filters={[]}
      table={
        <ClustersTable
          clusters={clusters?.entries || []}
          ariaLabel={"Cluster overview"}
        />
      }
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { Clusters };
