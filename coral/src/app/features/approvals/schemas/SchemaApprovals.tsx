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

function SchemaApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { environment, status, topic, filters } = useTableFilters();

  const [detailsModal, setDetailsModal] = useState<{
    isOpen: boolean;
    req_no: number | null;
  }>({ isOpen: false, req_no: null });

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

  const table = (
    <SchemaApprovalsTable
      requests={schemaRequests?.entries || []}
      setDetailsModal={setDetailsModal}
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

  function rejectRequest(req_no: number | null) {
    console.log("approve", req_no);
  }

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, req_no: null })}
          onApprove={() => {
            approveRequest(detailsModal.req_no);
          }}
          onReject={() => {
            setDetailsModal({ isOpen: false, req_no: null });
            rejectRequest(detailsModal.req_no);
          }}
          isLoading={false}
        >
          <SchemaRequestDetails
            request={schemaRequests?.entries.find(
              (request) => request.req_no === detailsModal.req_no
            )}
          />
        </RequestDetailsModal>
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
