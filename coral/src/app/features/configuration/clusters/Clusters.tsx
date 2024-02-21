import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { SearchClusterParamFilter } from "src/app/features/components/filters/SearchClusterParamFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import ClusterConnectHelpModal from "src/app/features/configuration/clusters/components/ClusterConnectHelpModal";
import { ClustersTable } from "src/app/features/configuration/clusters/components/ClustersTable";
import { ClusterDetails, getClustersPaginated } from "src/domain/cluster";

// We add this default data to avoid dealing with the fact that it may be undefined when closing the modal...
// ... and resetting the state
const DEFAULT_MODAL_DATA: ClusterModal["data"] = {
  kafkaFlavor: "AIVEN_FOR_APACHE_KAFKA",
  protocol: "PLAINTEXT",
  clusterType: "KAFKA",
  clusterName: "",
  clusterId: 0,
};

function Clusters() {
  const { permissions } = useAuthContext();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search } = useFiltersContext();

  const {
    data: clusters,
    isLoading,
    isError,
    error,
  } = useQuery(["get-clusters-paginated", currentPage, search], {
    queryFn: () =>
      getClustersPaginated({
        pageNo: currentPage.toString(),
        searchClusterParam: search.length === 0 ? undefined : search,
      }),
    keepPreviousData: true,
  });

  const [modal, setModal] = useState<ClusterModal>({
    show: false,
    data: DEFAULT_MODAL_DATA,
  });

  // Automatically open the Connect mhelp modal after adding a cluster.
  // We know how to populate the modal data from the fact that we redirect...
  // ... to a filtered table from the AddNewClusterForm, along with a specific query param
  // For example: /configuration/clusters?search=MyCluster&showConnectHelp=true
  useEffect(() => {
    const showConnectHelp = searchParams.get("showConnectHelp") !== null;
    const latestAddedCluster = showConnectHelp
      ? clusters?.entries[0]
      : undefined;
    const isShowingOnlyLatestAddedCluster =
      latestAddedCluster !== undefined && search.length !== 0;

    if (showConnectHelp && isShowingOnlyLatestAddedCluster) {
      const { kafkaFlavor, protocol, clusterType, clusterName, clusterId } =
        latestAddedCluster;

      handleShowModal({
        show: true,
        data: {
          kafkaFlavor,
          protocol,
          clusterType,
          clusterName,
          clusterId,
        },
      });
    }
  });

  function handleShowModal({ show, data }: ClusterModal) {
    setModal({
      show,
      data,
    });

    // If showConnectHelp is present, remove it on next call of handleShowModal
    // Most probably when closing the modal opened automatically after adding a cluster
    searchParams.delete("showConnectHelp");
    setSearchParams(searchParams);
  }

  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }

  const pagination =
    clusters && clusters.totalPages > 1 ? (
      <Pagination
        activePage={clusters.currentPage}
        totalPages={clusters.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  return (
    <>
      {modal.show && (
        <ClusterConnectHelpModal
          onClose={() =>
            setModal({
              show: false,
              data: DEFAULT_MODAL_DATA,
            })
          }
          modalData={modal.data}
        />
      )}

      <TableLayout
        filters={[<SearchClusterParamFilter key={"search"} />]}
        table={
          <ClustersTable
            clusters={clusters?.entries || []}
            handleShowModal={
              permissions.addDeleteEditClusters ? handleShowModal : undefined
            }
            ariaLabel={`Cluster overview, page ${clusters?.currentPage ?? 0} of ${
              clusters?.totalPages ?? 0
            }`}
          />
        }
        isLoading={isLoading}
        isErrorLoading={isError}
        errorMessage={error}
        pagination={pagination}
      />
    </>
  );
}

export interface ClusterModal {
  show: boolean;
  data: {
    kafkaFlavor: ClusterDetails["kafkaFlavor"];
    protocol: ClusterDetails["protocol"];
    clusterType: ClusterDetails["clusterType"];
    clusterName: ClusterDetails["clusterName"];
    clusterId: ClusterDetails["clusterId"];
  };
}

export default withFiltersContext({ element: <Clusters /> });
