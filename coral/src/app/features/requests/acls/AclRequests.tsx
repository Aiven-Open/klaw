import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import AclDetailsModalContent from "src/app/features/components/AclDetailsModalContent";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import AclTypeFilter from "src/app/features/components/table-filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/table-filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { AclRequestsTable } from "src/app/features/requests/acls/components/AclRequestsTable";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { deleteAclRequest, getAclRequests } from "src/domain/acl/acl-api";
import { AclRequest } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";
import { objectHasProperty } from "src/services/type-utils";

function AclRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? "";
  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentAclType =
    (searchParams.get("aclType") as AclRequest["aclType"]) ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as AclRequest["requestStatus"]) ?? "ALL";
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true" ? true : undefined;

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
      currentTopic,
      currentEnvironment,
      currentAclType,
      currentStatus,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getAclRequests({
        pageNo: String(currentPage),
        topic: currentTopic,
        env: currentEnvironment,
        aclType: currentAclType,
        requestStatus: currentStatus,
        isMyRequest: showOnlyMyRequests,
      }),
    keepPreviousData: true,
  });

  const [errorMessage, setErrorMessage] = useState("");

  const { isLoading: deleteIsLoading, mutate: deleteRequest } = useMutation({
    mutationFn: deleteAclRequest,
    onSuccess: async (responses) => {
      const response = responses[0];

      if (response.result !== "success") {
        closeModal();
        throw Error(response.message || response.result || "Unexpected error");
      }

      setErrorMessage("");

      // We need to refetch all aclrequests queries to keep Table state in sync
      // We await it to only close modal after refetch is done
      await queryClient.refetchQueries(["aclRequests"]);

      closeModal();
    },
    onError: (error: Error) => {
      // Case when error is from the server
      if (objectHasProperty(error, "data")) {
        return setErrorMessage(parseErrorMsg(error));
      }
      // Case when the error is thrown from onSuccess
      setErrorMessage(error.message);
    },
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
          <EnvironmentFilter key="environment" />,
          <AclTypeFilter key="aclType" />,
          <StatusFilter key="status" defaultStatus="ALL" />,
          <TopicFilter key="search" />,
          <MyRequestsFilter key="myRequests" />,
        ]}
        table={
          <AclRequestsTable
            requests={data?.entries ?? []}
            onDetails={openDetailsModal}
            onDelete={openDeleteModal}
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
