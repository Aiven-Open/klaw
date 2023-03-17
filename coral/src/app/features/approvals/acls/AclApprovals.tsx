import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import AclApprovalsTable from "src/app/features/approvals/acls/components/AclApprovalsTable";
import DetailsModalContent from "src/app/features/approvals/acls/components/DetailsModalContent";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import AclTypeFilter from "src/app/features/components/table-filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequestsForApprover, AclType } from "src/domain/acl/acl-types";
import { RequestStatus } from "src/domain/requests/requests-types";
import { parseErrorMsg } from "src/services/mutation-utils";

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  // This logic is what should be extracted in a useFilters hook?
  const currentAclType = (searchParams.get("aclType") as AclType) ?? "ALL";
  const currentEnv = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? "CREATED";
  const currentTopic = searchParams.get("topic") ?? "";

  const [detailsModal, setDetailsModal] = useState({
    isOpen: false,
    reqNo: "",
  });
  const [declineModal, setDeclineModal] = useState({
    isOpen: false,
    reqNo: "",
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
    queryKey: [
      "aclRequests",
      currentPage,
      currentAclType,
      currentEnv,
      currentStatus,
      currentTopic,
    ],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(currentPage),
        env: currentEnv,
        requestStatus: currentStatus,
        aclType: currentAclType,
        topic: currentTopic,
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
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      const response = responses[0];
      if (response.result !== "success") {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDetailsModal({ isOpen: false, reqNo: "" });

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

  const { isLoading: declineIsLoading, mutate: declineRequest } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: (responses) => {
      const response = responses[0];
      if (response.result !== "success") {
        return setErrorMessage(
          response.message || response.result || "Unexpected error"
        );
      }
      setErrorMessage("");
      setDeclineModal({ isOpen: false, reqNo: "" });

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

  function handleViewRequest(reqNo: string): void {
    setDetailsModal({ isOpen: true, reqNo });
  }

  function handleApproveRequest(reqNo: string): void {
    approveRequest({
      requestEntityType: "ACL",
      reqIds: [reqNo],
    });
  }

  function handleDeclineRequest(reqNo: string): void {
    setDeclineModal({ isOpen: true, reqNo });
  }

  function handleIsBeingApproved(reqNo: string): boolean {
    return Boolean(approveVariables?.reqIds?.includes(reqNo));
  }

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: "" })}
          actions={{
            primary: {
              text: "Approve",
              onClick: () => {
                approveRequest({
                  requestEntityType: "ACL",
                  reqIds: [detailsModal.reqNo],
                });
              },
            },
            secondary: {
              text: "Decline",
              onClick: () => {
                setDetailsModal({ isOpen: false, reqNo: "" });
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
          <DetailsModalContent aclRequest={selectedRequest} />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={() => setDeclineModal({ isOpen: false, reqNo: "" })}
          onCancel={() => setDeclineModal({ isOpen: false, reqNo: "" })}
          onSubmit={(message: string) => {
            declineRequest({
              requestEntityType: "ACL",
              reqIds: [declineModal.reqNo],
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
            isBeingApproved={handleIsBeingApproved}
            onDetails={handleViewRequest}
            onApprove={handleApproveRequest}
            onDecline={handleDeclineRequest}
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
