import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import ConnectorApprovalsTable from "src/app/features/approvals/connectors/components/ConnectorApprovalsTable";
import { ConnectorRequestDetails } from "src/app/features/components/ConnectorDetailsModalContent";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import SearchFilter from "src/app/features/components/filters/SearchFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import {
  approveConnectorRequest,
  declineConnectorRequest,
  getConnectorRequestsForApprover,
} from "src/domain/connector";
import { parseErrorMsg } from "src/services/mutation-utils";

const defaultType = "ALL";

function ConnectorApprovals() {
  const queryClient = useQueryClient();

  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { environment, status, search, requestType } = useFiltersValues({
    defaultStatus: "CREATED",
  });

  const [detailsModal, setDetailsModal] = useState<{
    isOpen: boolean;
    connectorId: number | null;
  }>({
    isOpen: false,
    connectorId: null,
  });
  const [declineModal, setDeclineModal] = useState<{
    isOpen: boolean;
    connectorId: number | null;
  }>({
    isOpen: false,
    connectorId: null,
  });

  const [errorQuickActions, setErrorQuickActions] = useState("");

  const {
    data: connectorRequests,
    isLoading: connectorRequestsIsLoading,
    isError: connectorRequestsIsError,
    error: connectorRequestsError,
  } = useQuery({
    queryKey: [
      "connectorRequestsForApprover",
      currentPage,
      status,
      environment,
      search,
      requestType,
    ],
    queryFn: () =>
      getConnectorRequestsForApprover({
        requestStatus: status,
        pageNo: currentPage.toString(),
        env: environment,
        search: search,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  const {
    mutate: declineRequest,
    isLoading: declineIsLoading,
    variables: declineVariables,
  } = useMutation(declineConnectorRequest, {
    onSuccess: () => {
      setErrorQuickActions("");
      // Re-fetch to update the tag number in the tabs
      queryClient.refetchQueries(["connectorRequestsForApprover"]);

      // If declined request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (
        connectorRequests?.entries.length === 1 &&
        connectorRequests?.currentPage > 1
      ) {
        // setting the current page will the api call to re-fetch the connector requests
        setCurrentPage(connectorRequests?.currentPage - 1);
      } else {
        // We need to refetch the connector requests to keep Table state in sync
        queryClient.refetchQueries(["connectorRequestsForApprover"]);
      }
    },
    onError(error: unknown) {
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
  } = useMutation(approveConnectorRequest, {
    onSuccess: () => {
      setErrorQuickActions("");
      setDetailsModal({ isOpen: false, connectorId: null });
      setDeclineModal({ isOpen: false, connectorId: null });

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If declined request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (
        connectorRequests?.entries.length === 1 &&
        connectorRequests?.currentPage > 1
      ) {
        return setCurrentPage(connectorRequests?.currentPage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["connectorRequestsForApprover"]);
    },
    onError(error: Error) {
      setErrorQuickActions(parseErrorMsg(error));
    },
    onSettled() {
      closeModal();
    },
  });

  function closeModal() {
    setDetailsModal({ isOpen: false, connectorId: null });
    setDeclineModal({ isOpen: false, connectorId: null });
  }

  function handleViewRequest(connectorId: number): void {
    setDetailsModal({ isOpen: true, connectorId });
  }

  function handleApproveRequest(connectorId: number): void {
    approveRequest({
      reqIds: [String(connectorId)],
    });
  }

  function handleDeclineRequest(connectorId: number): void {
    setDeclineModal({ isOpen: true, connectorId });
  }

  function handleIsBeingApproved(connectorId: number): boolean {
    return (
      Boolean(approveVariables?.reqIds?.includes(String(connectorId))) &&
      approveIsLoading
    );
  }

  function handleIsBeingDeclined(connectorId: number): boolean {
    return (
      Boolean(declineVariables?.reqIds?.includes(String(connectorId))) &&
      declineIsLoading
    );
  }

  const table = (
    <ConnectorApprovalsTable
      requests={connectorRequests?.entries || []}
      actionsDisabled={approveIsLoading || declineIsLoading}
      onDetails={handleViewRequest}
      onApprove={handleApproveRequest}
      onDecline={handleDeclineRequest}
      isBeingDeclined={handleIsBeingDeclined}
      isBeingApproved={handleIsBeingApproved}
      ariaLabel={`Connector approval requests, page ${
        connectorRequests?.currentPage ?? 0
      } of ${connectorRequests?.totalPages ?? 0}`}
    />
  );

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    connectorRequests?.totalPages && connectorRequests.totalPages > 1 ? (
      <Pagination
        activePage={connectorRequests.currentPage}
        totalPages={connectorRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  console.log("connectorRequests", connectorRequests);

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={closeModal}
          actions={{
            primary: {
              text: "Approve",
              onClick: () => {
                if (detailsModal.connectorId === null) {
                  throw Error("connectorId can't be null");
                }
                approveRequest({ reqIds: [String(detailsModal.connectorId)] });
              },
            },
            secondary: {
              text: "Decline",
              onClick: () => {
                setDetailsModal({ isOpen: false, connectorId: null });
                setDeclineModal({
                  isOpen: true,
                  connectorId: detailsModal.connectorId,
                });
              },
            },
          }}
          isLoading={declineIsLoading || approveIsLoading}
          disabledActions={declineIsLoading || approveIsLoading}
        >
          <ConnectorRequestDetails
            request={connectorRequests?.entries.find(
              (request) => request.connectorId === detailsModal.connectorId
            )}
          />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={closeModal}
          onCancel={closeModal}
          onSubmit={(message: string) => {
            if (declineModal.connectorId === null) {
              throw Error("connectorId can't be null");
            }
            declineRequest({
              reason: message,
              reqIds: [String(declineModal.connectorId)],
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
            environmentEndpoint={"getSyncConnectorsEnvironments"}
          />,
          <StatusFilter key={"status"} defaultStatus={"CREATED"} />,
          <RequestTypeFilter key={"requestType"} />,
          <SearchFilter key={"search"} />,
        ]}
        table={table}
        pagination={pagination}
        isLoading={connectorRequestsIsLoading}
        isErrorLoading={connectorRequestsIsError}
        errorMessage={connectorRequestsError}
      />
    </>
  );
}

export default ConnectorApprovals;
