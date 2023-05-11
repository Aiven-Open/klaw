import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import AclDetailsModalContent from "src/app/features/components/AclDetailsModalContent";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { AclRequestsTable } from "src/app/features/requests/acls/components/AclRequestsTable";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { deleteAclRequest, getAclRequests } from "src/domain/acl/acl-api";
import { parseErrorMsg } from "src/services/mutation-utils";

function AclRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const {
    search,
    environment,
    aclType,
    status,
    showOnlyMyRequests,
    requestType,
  } = useFiltersValues();

  const [modals, setModals] = useState<{
    open: "DETAILS" | "DELETE" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });

  const closeModal = () => {
    setModals({ open: "NONE", req_no: null });
  };

  const openDetailsModal = (req_no: number) => {
    setModals({ open: "DETAILS", req_no });
  };

  const openDeleteModal = (req_no: number) => {
    setModals({ open: "DELETE", req_no });
  };

  const {
    data,
    isLoading: dataIsLoading,
    isRefetching: dataIsRefetching,
    isError,
    error,
  } = useQuery({
    queryKey: [
      "aclRequests",
      currentPage,
      search,
      environment,
      aclType,
      status,
      requestType,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getAclRequests({
        pageNo: String(currentPage),
        search: search,
        env: environment,
        aclType,
        requestStatus: status,
        operationType: requestType === "ALL" ? undefined : requestType,
        isMyRequest: showOnlyMyRequests,
      }),
    keepPreviousData: true,
  });

  const [errorMessage, setErrorMessage] = useState("");

  const { isLoading: deleteIsLoading, mutate: deleteRequest } = useMutation({
    mutationFn: deleteAclRequest,
    onSuccess: async () => {
      setErrorMessage("");
      // We need to refetch all aclrequests queries to keep Table state in sync
      // We await it to only close modal after refetch is done
      await queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
    onSettled: closeModal,
  });

  const handleChangePage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const handleDelete = (reqNo: number | null) => {
    if (reqNo === null) {
      throw Error("Cannot delete request with req_no null");
    }
    deleteRequest({ reqIds: [String(reqNo)] });
  };

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const selectedRequest = data?.entries.find(
    (request) => request.req_no === modals.req_no
  );

  return (
    <>
      {modals.open === "DETAILS" && modals.req_no !== null && (
        <RequestDetailsModal
          onClose={closeModal}
          actions={{
            primary: {
              text: "Close",
              onClick: () => {
                if (modals.req_no === null) {
                  throw Error("req_no can't be null");
                }
                closeModal();
              },
            },
            secondary: {
              text: "Delete",
              onClick: () => {
                if (modals.req_no === null) {
                  throw Error("req_no can't be null");
                }
                openDeleteModal(modals.req_no);
              },
              disabled: !selectedRequest?.deletable,
            },
          }}
          isLoading={false}
          disabledActions={deleteIsLoading}
        >
          <AclDetailsModalContent request={selectedRequest} />
        </RequestDetailsModal>
      )}
      {modals.open === "DELETE" && modals.req_no !== null && (
        <DeleteRequestDialog
          cancel={closeModal}
          deleteRequest={() => handleDelete(modals.req_no)}
          isLoading={deleteIsLoading || dataIsRefetching}
        />
      )}
      {errorMessage !== "" && (
        <div role="alert">
          <Alert type="error">{errorMessage}</Alert>
        </div>
      )}
      <TableLayout
        filters={[
          <EnvironmentFilter
            key="environment"
            environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
          />,
          <AclTypeFilter key="aclType" />,
          <StatusFilter key="status" defaultStatus="ALL" />,
          <RequestTypeFilter key="operationType" />,
          <SearchTopicFilter key="search" />,
          <MyRequestsFilter key="myRequests" />,
        ]}
        table={
          <AclRequestsTable
            requests={data?.entries ?? []}
            onDetails={openDetailsModal}
            onDelete={openDeleteModal}
            ariaLabel={`ACL requests, page ${data?.currentPage ?? 0} of ${
              data?.totalPages ?? 0
            }`}
          />
        }
        pagination={pagination}
        isLoading={dataIsLoading}
        isErrorLoading={isError}
        errorMessage={error}
      />
    </>
  );
}

export { AclRequests };
