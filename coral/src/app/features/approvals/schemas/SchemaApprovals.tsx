import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/components/SchemaRequestDetails";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { RequestStatus } from "src/domain/requests/requests-types";
import {
  approveSchemaRequest,
  declineSchemaRequest,
  getSchemaRequestsForApprover,
} from "src/domain/schema-request";
import { parseErrorMsg } from "src/services/mutation-utils";

function SchemaApprovals() {
  const queryClient = useQueryClient();

  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  // This logic is what should be extracted in a useFilters hook?
  const currentEnv = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? "CREATED";
  const currentTopic = searchParams.get("topic") ?? "";

  const [detailsModal, setDetailsModal] = useState<{
    isOpen: boolean;
    reqNo: number | null;
  }>({
    isOpen: false,
    reqNo: null,
  });
  const [declineModal, setDeclineModal] = useState<{
    isOpen: boolean;
    reqNo: number | null;
  }>({
    isOpen: false,
    reqNo: null,
  });

  const [errorQuickActions, setErrorQuickActions] = useState("");

  const {
    data: schemaRequests,
    isLoading: schemaRequestsIsLoading,
    isError: schemaRequestsIsError,
    error: schemaRequestsError,
  } = useQuery({
    queryKey: [
      "schemaRequestsForApprover",
      currentPage,
      currentStatus,
      currentEnv,
      currentTopic,
    ],
    queryFn: () =>
      getSchemaRequestsForApprover({
        requestStatus: currentStatus,
        pageNo: currentPage.toString(),
        env: currentEnv,
        topic: currentTopic,
      }),
    keepPreviousData: true,
  });

  const {
    mutate: declineRequest,
    isLoading: declineIsLoading,
    variables: declineVariables,
  } = useMutation(declineSchemaRequest, {
    onSuccess: (responses) => {
      // @TODO follow up ticket #707
      // (for all approval tables)
      const response = responses[0];
      const responseIsAHiddenError =
        response?.result.toLowerCase() !== "success";
      if (responseIsAHiddenError) {
        return setErrorQuickActions(
          response.message || response.result || "Unexpected error"
        );
      }

      setErrorQuickActions("");
      setDetailsModal({ isOpen: false, reqNo: null });
      setDeclineModal({ isOpen: false, reqNo: null });

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If declined request is last in the page, go back to previous page
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
    },
    onError(error: Error) {
      setErrorQuickActions(parseErrorMsg(error));
    },
    onSettled() {
      closeModal();
    },
  });

  const {
    mutate: approveRequest,
    isLoading: approveIsLoading,
    variables: approveVariables,
  } = useMutation(approveSchemaRequest, {
    onSuccess: (responses) => {
      // @TODO follow up ticket #707
      // (for all approval tables)
      const response = responses[0];
      const responseIsAHiddenError =
        response?.result.toLowerCase() !== "success";
      if (responseIsAHiddenError) {
        return setErrorQuickActions(
          response.message || response.result || "Unexpected error"
        );
      }

      setErrorQuickActions("");
      setDetailsModal({ isOpen: false, reqNo: null });
      setDeclineModal({ isOpen: false, reqNo: null });

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If declined request is last in the page, go back to previous page
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
    setDetailsModal({ isOpen: false, reqNo: null });
    setDeclineModal({ isOpen: false, reqNo: null });
  }

  function handleViewRequest(reqNo: number): void {
    setDetailsModal({ isOpen: true, reqNo });
  }

  function handleApproveRequest(reqNo: number): void {
    approveRequest({
      reqIds: [String(reqNo)],
    });
  }

  function handleDeclineRequest(reqNo: number): void {
    setDeclineModal({ isOpen: true, reqNo });
  }

  function handleIsBeingApproved(reqNo: number): boolean {
    return (
      Boolean(approveVariables?.reqIds?.includes(String(reqNo))) &&
      approveIsLoading
    );
  }

  function handleIsBeingDeclined(reqNo: number): boolean {
    return (
      Boolean(declineVariables?.reqIds?.includes(String(reqNo))) &&
      declineIsLoading
    );
  }

  const table = (
    <SchemaApprovalsTable
      requests={schemaRequests?.entries || []}
      actionsDisabled={approveIsLoading || declineIsLoading}
      onDetails={handleViewRequest}
      onApprove={handleApproveRequest}
      onDecline={handleDeclineRequest}
      isBeingDeclined={handleIsBeingDeclined}
      isBeingApproved={handleIsBeingApproved}
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

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={closeModal}
          actions={{
            primary: {
              text: "Approve",
              onClick: () => {
                if (detailsModal.reqNo === null) {
                  throw Error("req_no can't be null");
                }
                approveRequest({ reqIds: [String(detailsModal.reqNo)] });
              },
            },
            secondary: {
              text: "Decline",
              onClick: () => {
                setDetailsModal({ isOpen: false, reqNo: null });
                setDeclineModal({
                  isOpen: true,
                  reqNo: detailsModal.reqNo,
                });
              },
            },
          }}
          isLoading={declineIsLoading || approveIsLoading}
          disabledActions={declineIsLoading || approveIsLoading}
        >
          <SchemaRequestDetails
            request={schemaRequests?.entries.find(
              (request) => request.req_no === detailsModal.reqNo
            )}
          />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={() => closeModal()}
          onCancel={() => closeModal()}
          onSubmit={(message: string) => {
            if (declineModal.reqNo === null) {
              throw Error("req_no can't be null");
            }
            declineRequest({
              reason: message,
              reqIds: [String(declineModal.reqNo)],
            });
          }}
          isLoading={declineIsLoading || approveIsLoading}
        />
      )}
      {errorQuickActions && (
        <div role="alert">
          <Alert type="error">{errorQuickActions}</Alert>
        </div>
      )}
      <TableLayout
        filters={[
          <EnvironmentFilter
            key={"environment"}
            isSchemaRegistryEnvironments
          />,
          <StatusFilter key={"status"} defaultStatus={"CREATED"} />,
          <TopicFilter key={"topic"} />,
        ]}
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
