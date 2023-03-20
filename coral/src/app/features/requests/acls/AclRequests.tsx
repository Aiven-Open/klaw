import { useQuery } from "@tanstack/react-query";
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
import { getAclRequests } from "src/domain/acl/acl-api";
import { AclRequest } from "src/domain/acl/acl-types";

function AclRequests() {
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

  const { data, isLoading, isError, error } = useQuery({
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

  const handleChangePage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
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
        >
          <AclDetailsModalContent request={selectedRequest} />
        </RequestDetailsModal>
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
        isLoading={isLoading}
        isErrorLoading={isError}
        errorMessage={error}
      />
    </>
  );
}

export { AclRequests };
