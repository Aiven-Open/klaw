import { Pagination } from "src/app/components/Pagination";
import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { getSchemaRequestsForApprover } from "src/domain/schema-request";
import { useSearchParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import useTableFilters from "src/app/features/approvals/schemas/hooks/useTableFilters";
import { useState } from "react";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/approvals/schemas/components/SchemaRequestDetails";
import RequestRejectModal from "src/app/features/approvals/components/RequestRejectModal";
import { declineSchemaRequest } from "src/domain/schema-request/schema-request-api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { Alert } from "@aivenio/aquarium";

function SchemaApprovals() {
  const queryClient = useQueryClient();

  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const [modals, setModals] = useState<{
    open: "DETAILS" | "REJECT" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });

  const [errorQuickActions, setErrorQuickActions] = useState("");

  const { environment, status, topic, filters } = useTableFilters();

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
    onSuccess: (newSchemaRequests) => {
      // If through filtering a user finds themselves on a non existent page, reset page to 1
      // For example:
      // - one request returns 4 pages of results
      // - navigate to page 4
      // - change filters, to a request that returns 1 page of results
      // - if not redirected to page 1, table won't be able to handle pagination (clicking "Back" will set page at -1)
      if (
        newSchemaRequests.entries.length === 0 &&
        schemaRequests?.currentPage !== 1
      ) {
        setCurrentPage(1);
      }
    },
    keepPreviousData: true,
  });

  const { mutate: declineRequest, isLoading: rejectRequestIsLoading } =
    useMutation(declineSchemaRequest, {
      onSuccess: (responses) => {
        // @TODO follow up ticket #707
        // (for all approval tables)
        const response = responses[0];
        if (response.result !== "success") {
          setErrorQuickActions(
            response.message || response.result || "Unexpected error"
          );
        } else {
          setErrorQuickActions("");
          // If rejected request is last in the page, go back to previous page
          // This avoids staying on a non-existent page of entries, which makes the table bug hard
          // With pagination being 0 of 0, and clicking Previous button sets active page at -1
          // We also do not need to invalidate the query, as the activePage does not exist any more
          // And there is no need to update anything on it
          if (
            schemaRequests?.entries.length === 1 &&
            schemaRequests?.currentPage > 1
          ) {
            return setCurrentPage(schemaRequests?.currentPage - 1);
          }

          // We need to refetch all aclrequests queries to keep Table state in sync
          queryClient.refetchQueries(["schemaRequestsForApprover"]);
        }
      },
      onError(error: Error) {
        setErrorQuickActions(parseErrorMsg(error));
      },
      onSettled() {
        closeModal();
      },
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
            declineRequest({
              reason: message,
              reqIds: [modals.req_no.toString()],
            });
          }}
          isLoading={rejectRequestIsLoading}
        />
      )}
      {errorQuickActions && (
        <div role="alert">
          <Alert type="warning">{errorQuickActions}</Alert>
        </div>
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
