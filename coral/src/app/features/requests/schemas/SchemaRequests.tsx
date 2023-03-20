import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/components/SchemaRequestDetails";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/table-filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { SchemaRequestTable } from "src/app/features/requests/schemas/components/SchemaRequestTable";
import { RequestStatus } from "src/domain/requests/requests-types";
import { getSchemaRequests } from "src/domain/schema-request";

const defaultStatus = "ALL";

function SchemaRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? undefined;
  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? defaultStatus;
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true" ? true : undefined;

  const [modals, setModals] = useState<{
    open: "DETAILS" | "DELETE" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });

  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: [
      "schemaRequests",
      currentPage,
      currentEnvironment,
      currentStatus,
      currentTopic,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getSchemaRequests({
        pageNo: String(currentPage),
        env: currentEnvironment,
        requestStatus: currentStatus,
        topic: currentTopic,
        isMyRequest: showOnlyMyRequests,
      }),
    keepPreviousData: true,
  });

  function closeModal() {
    setModals({ open: "NONE", req_no: null });
  }

  const openDetailsModal = (req_no: number) => {
    setModals({ open: "DETAILS", req_no });
  };

  function setCurrentPage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }

  const pagination =
    schemaRequests?.totalPages && schemaRequests.totalPages > 1 ? (
      <Pagination
        activePage={schemaRequests.currentPage}
        totalPages={schemaRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  return (
    <>
      {modals.open === "DETAILS" && (
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
                console.log("DELETE");
              },
            },
          }}
          isLoading={false}
        >
          <SchemaRequestDetails
            request={schemaRequests?.entries.find(
              (request) => request.req_no === modals.req_no
            )}
          />
        </RequestDetailsModal>
      )}

      <TableLayout
        filters={[
          <EnvironmentFilter
            key={"environments"}
            isSchemaRegistryEnvironments
          />,
          <StatusFilter key={"request-status"} defaultStatus={defaultStatus} />,
          <TopicFilter key={"topic"} />,
          <MyRequestsFilter key={"show-only-my-requests"} />,
        ]}
        table={
          <SchemaRequestTable
            requests={schemaRequests?.entries || []}
            showDetails={openDetailsModal}
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

export { SchemaRequests };
