import { Pagination } from "src/app/components/Pagination";
import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { getSchemaRequestsForApprover } from "src/domain/schema-request";
import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import useTableFilters from "src/app/features/approvals/schemas/hooks/useTableFilters";
import { useState } from "react";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/approvals/schemas/components/SchemaRequestDetails";
import RequestRejectModal from "src/app/features/approvals/components/RequestRejectModal";

function SchemaApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { environment, status, topic, filters } = useTableFilters();

  const [modals, setModals] = useState<{
    open: "DETAILS" | "REJECT" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });

  const {
    data: schemaRequests,
    isLoading: schemaRequestsIsLoading,
    isError: schemaRequestsIsError,
    error: schemaRequestsError,
  } = useQuery({
    queryKey: [
      "schemaRequestsForApprover",
      currentPage,
      status,
      environment,
      topic,
    ],
    queryFn: () =>
      getSchemaRequestsForApprover({
        requestStatus: status,
        pageNo: currentPage.toString(),
        env: environment,
        topic,
      }),
    keepPreviousData: true,
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  function closeModal() {
    setModals({ open: "NONE", req_no: null });
  }

  const table = (
    <SchemaApprovalsTable
      requests={schemaRequests?.entries || []}
      setModals={setModals}
    />
  );
  const pagination =
    schemaRequests?.totalPages && schemaRequests.totalPages > 1 ? (
      <Pagination
        activePage={schemaRequests.currentPage}
        totalPages={schemaRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  function approveRequest(req_no: number | null) {
    console.log("approve", req_no);
  }

  function declineRequest(req_no: number | null) {
    console.log("approve", req_no);
  }

  return (
    <>
      {modals.open === "DETAILS" && (
        <RequestDetailsModal
          onClose={closeModal}
          onApprove={() => {
            //api call
            console.log("APPROVE", modals.req_no);
            closeModal();
          }}
          onDecline={() => {
            setModals({ open: "NONE", req_no: null });
            declineRequest(modals.req_no);
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
      {modals.open === "REJECT" && (
        <RequestRejectModal
          onClose={() => closeModal()}
          onCancel={() => closeModal()}
          onSubmit={(message: string) => {
            if (modals.req_no === null) {
              throw Error("req_no can't be null");
            }
            //do api call
            alert(`rejected nr ${modals.req_no} for reason: ${message}`);
            closeModal();
          }}
          isLoading={false}
        />
      )}
      <ApprovalsLayout
        filters={filters}
        table={table}
        pagination={pagination}
        isLoading={schemaRequestsIsLoading}
        isErrorLoading={schemaRequestsIsError}
        errorMessage={schemaRequestsError}
      />
    </>
  );
}

export default SchemaApprovals;
