import { useToast } from "@aivenio/aquarium";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { ClusterTypeFilter } from "src/app/features/components/filters/ClusterTypeFilter";
import { SearchClusterParamFilter } from "src/app/features/components/filters/SearchClusterParamFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import ClusterConnectHelpModal from "src/app/features/configuration/clusters/components/ClusterConnectHelpModal";
import ClusterDeleteModalState from "src/app/features/configuration/clusters/components/ClusterDeleteModal";
import { ClustersTable } from "src/app/features/configuration/clusters/components/ClustersTable";
import {
  ClusterDetails,
  deleteCluster,
  getClustersPaginated,
} from "src/domain/cluster";

// We add this default data to avoid dealing with the fact that it may be undefined when closing the modal...
// ... and resetting the state
const DEFAULT_CONNECT_MODAL_DATA: ClusterConnectModalState["data"] = {
  kafkaFlavor: "AIVEN_FOR_APACHE_KAFKA",
  protocol: "PLAINTEXT",
  clusterType: "KAFKA",
  clusterName: "",
  clusterId: 0,
};

const INITIAL_DELETE_MODAL_DATA: ClusterDeleteModalState["data"] = {
  clusterId: 0,
  clusterName: "",
  canDeleteCluster: false,
};

function Clusters() {
  const toast = useToast();
  const { permissions } = useAuthContext();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, clusterType } = useFiltersContext();

  const {
    mutateAsync: deleteClusterAction,
    isLoading: deleteIsLoading,
    isSuccess: deleteIsSuccess,
    error: deleteError,
  } = useMutation(deleteCluster, {
    onSuccess: () => {
      toast({
        message: `Cluster ${deleteModal.data.clusterName} successfully removed`,
        position: "bottom-left",
        variant: "default",
      });

      setDeleteModal({
        show: false,
        data: INITIAL_DELETE_MODAL_DATA,
      });
    },
  });

  const {
    data: clusters,
    isLoading,
    isError,
    error,
  } = useQuery(
    [
      "get-clusters-paginated",
      currentPage,
      search,
      clusterType,
      deleteIsSuccess,
    ],
    {
      queryFn: () =>
        getClustersPaginated({
          clusterType,
          pageNo: currentPage.toString(),
          searchClusterParam: search.length === 0 ? undefined : search,
        }),
      keepPreviousData: true,
    }
  );

  const [connectModal, setConnectModal] = useState<ClusterConnectModalState>({
    show: false,
    data: DEFAULT_CONNECT_MODAL_DATA,
  });
  const [deleteModal, setDeleteModal] = useState<ClusterDeleteModalState>({
    show: false,
    data: INITIAL_DELETE_MODAL_DATA,
  });

  // Automatically open the Connect help modal after adding a cluster.
  // We know how to populate the modal data from the fact that we redirect...
  // ... to a filtered table from the AddNewClusterForm, along with a specific query param
  // For example: /configuration/clusters?search=MyCluster&showConnectHelp=true
  useEffect(() => {
    const showConnectHelp = searchParams.get("showConnectHelp") !== null;
    const latestAddedCluster = showConnectHelp
      ? clusters?.entries[0]
      : undefined;
    const isShowingOnlyLatestAddedCluster =
      // If there is more than one cluster resulting from the searchClusterParam filter, we should not show the modal
      // As we currently can't know which one was the latest added
      clusters?.entries.length === 1 &&
      latestAddedCluster !== undefined &&
      search.length !== 0;

    if (showConnectHelp && isShowingOnlyLatestAddedCluster) {
      const { kafkaFlavor, protocol, clusterType, clusterName, clusterId } =
        latestAddedCluster;

      handleShowConnectModal({
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

  function handleShowConnectModal({ show, data }: ClusterConnectModalState) {
    setConnectModal({
      show,
      data,
    });

    // If showConnectHelp is present, remove it on next call of handleShowModal
    // Most probably when closing the modal opened automatically after adding a cluster
    searchParams.delete("showConnectHelp");
    setSearchParams(searchParams);
  }

  function handleShowDeleteModal({ show, data }: ClusterDeleteModalState) {
    setDeleteModal({
      show,
      data,
    });
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
      {connectModal.show && (
        <ClusterConnectHelpModal
          onClose={() =>
            setConnectModal({
              show: false,
              data: DEFAULT_CONNECT_MODAL_DATA,
            })
          }
          modalData={connectModal.data}
        />
      )}
      {deleteModal.show && (
        <ClusterDeleteModalState
          handleDelete={(clusterId: string) => deleteClusterAction(clusterId)}
          handleClose={() =>
            setDeleteModal({
              show: false,
              data: INITIAL_DELETE_MODAL_DATA,
            })
          }
          isLoading={deleteIsLoading}
          error={deleteError}
          clusterId={deleteModal.data.clusterId}
          clusterName={deleteModal.data.clusterName}
          canDeleteCluster={deleteModal.data.canDeleteCluster}
        />
      )}

      <TableLayout
        filters={[
          <ClusterTypeFilter key={"clusterType"} />,
          <SearchClusterParamFilter key={"search"} />,
        ]}
        table={
          <ClustersTable
            clusters={clusters?.entries || []}
            handleShowConnectModal={
              permissions.addDeleteEditClusters
                ? handleShowConnectModal
                : undefined
            }
            handleShowDeleteModal={
              permissions.addDeleteEditClusters
                ? handleShowDeleteModal
                : undefined
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

interface ClusterConnectModalState {
  show: boolean;
  data: {
    kafkaFlavor: ClusterDetails["kafkaFlavor"];
    protocol: ClusterDetails["protocol"];
    clusterType: ClusterDetails["clusterType"];
    clusterName: ClusterDetails["clusterName"];
    clusterId: ClusterDetails["clusterId"];
  };
}

interface ClusterDeleteModalState {
  show: boolean;
  data: {
    canDeleteCluster: ClusterDetails["showDeleteCluster"];
    clusterName: ClusterDetails["clusterName"];
    clusterId: ClusterDetails["clusterId"];
  };
}

export type { ClusterConnectModalState, ClusterDeleteModalState };
export default withFiltersContext({ element: <Clusters /> });
