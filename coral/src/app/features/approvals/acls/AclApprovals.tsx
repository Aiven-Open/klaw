import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import AclApprovalsTable from "src/app/features/approvals/acls/components/AclApprovalsTable";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import AclDetailsModalContent from "src/app/features/components/AclDetailsModalContent";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import TopicFilter from "src/app/features/components/filters/TopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequestsForApprover } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { aclType, environment, status, topic } = useFiltersValues({
    defaultStatus: "CREATED",
  });

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

  const [errorMessage, setErrorMessage] = useState("");

  const handleChangePage = (activePage: number) => {
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const { data, isLoading, isError, error } = useQuery<
    AclRequestsForApprover,
    Error
  >({
    queryKey: ["aclRequests", currentPage, aclType, environment, status, topic],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(currentPage),
        env: environment,
        requestStatus: status,
        aclType,
        topic,
      }),
    keepPreviousData: true,
  });

  const {
    isLoading: approveIsLoading,
    mutate: approveRequest,
    variables: approveVariables,
  } = useMutation({
    mutationFn: approveAclRequest,
    onSuccess: (responses) => {
      const response = responses[0];
      const responseIsAHiddenError =
        response?.result.toLowerCase() !== "success";
      if (responseIsAHiddenError) {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDetailsModal({ isOpen: false, reqNo: null });

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        return handleChangePage(currentPage - 1);
      }

      // Refetch to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const {
    isLoading: declineIsLoading,
    mutate: declineRequest,
    variables: declineVariables,
  } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: (responses) => {
      const response = responses[0];
      const responseIsAHiddenError =
        response?.result.toLowerCase() !== "success";
      if (responseIsAHiddenError) {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDeclineModal({ isOpen: false, reqNo: null });

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        return handleChangePage(currentPage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
  });

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const selectedRequest = data?.entries.find(
    (request) => request.req_no === Number(detailsModal.reqNo)
  );

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

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: null })}
          actions={{
            primary: {
              text: "Approve",
              onClick: () => {
                approveRequest({
                  reqIds: [String(detailsModal.reqNo)],
                });
              },
            },
            secondary: {
              text: "Decline",
              onClick: () => {
                setDetailsModal({ isOpen: false, reqNo: null });
                setDeclineModal({ isOpen: true, reqNo: detailsModal.reqNo });
              },
            },
          }}
          isLoading={approveIsLoading || declineIsLoading}
          disabledActions={
            selectedRequest?.requestStatus !== "CREATED" ||
            approveIsLoading ||
            declineIsLoading
          }
        >
          <AclDetailsModalContent request={selectedRequest} />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={() => setDeclineModal({ isOpen: false, reqNo: null })}
          onCancel={() => setDeclineModal({ isOpen: false, reqNo: null })}
          onSubmit={(message: string) => {
            declineRequest({
              reqIds: [String(declineModal.reqNo)],
              reason: message,
            });
          }}
          isLoading={declineIsLoading}
        />
      )}
      {errorMessage !== "" && (
        <div role="alert">
          <Alert type="error">{errorMessage}</Alert>
        </div>
      )}

      <TableLayout
        filters={[
          <EnvironmentFilter key={"environment"} />,
          <StatusFilter key={"status"} defaultStatus={"CREATED"} />,
          <AclTypeFilter key={"aclType"} />,
          <TopicFilter key={"topic"} />,
        ]}
        table={
          <AclApprovalsTable
            aclRequests={data?.entries ?? []}
            activePage={data?.currentPage ?? 1}
            totalPages={data?.totalPages ?? 1}
            actionsDisabled={approveIsLoading || declineIsLoading}
            onDetails={handleViewRequest}
            onApprove={handleApproveRequest}
            onDecline={handleDeclineRequest}
            isBeingDeclined={handleIsBeingDeclined}
            isBeingApproved={handleIsBeingApproved}
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

export default AclApprovals;
