import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import DetailsModalContent from "src/app/features/approvals/acls/components/DetailsModalContent";
import useTableFilters from "src/app/features/approvals/acls/hooks/useTableFilters";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequestsForApprover } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";
import AclApprovalsTable from "src/app/features/approvals/acls/components/AclApprovalsTable";

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialPage = Number(searchParams.get("page"));
  const [activePage, setActivePage] = useState(initialPage || 1);

  const [detailsModal, setDetailsModal] = useState({
    isOpen: false,
    reqNo: "",
  });
  const [declineModal, setDeclineModal] = useState({
    isOpen: false,
    reqNo: "",
  });

  const [errorMessage, setErrorMessage] = useState("");

  const { environment, status, aclType, topic, filters } = useTableFilters();

  const handleChangePage = (activePage: number) => {
    setActivePage(activePage);
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const { data, isLoading, isError, error } = useQuery<
    AclRequestsForApprover,
    Error
  >({
    queryKey: ["aclRequests", activePage, environment, status, aclType, topic],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(activePage),
        env: environment,
        requestStatus: status,
        aclType,
        topic,
      }),
    onSuccess: (data) => {
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);
      // If through filtering a user finds themselves on a non existent page, reset page to 1
      // For example:
      // - one request returns 4 pages of results
      // - navigate to page 4
      // - change filters, to a request that returns 1 page of results
      // - if not redirected to page 1, table won't be able to handle pagination (clicking "Back" will set page at -1)
      if (data.entries.length === 0 && activePage !== 1) {
        handleChangePage(1);
      }
    },
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
        return handleChangePage(activePage - 1);
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
        return handleChangePage(activePage - 1);
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
          onApprove={() => {
            approveRequest({
              requestEntityType: "ACL",
              reqIds: [detailsModal.reqNo],
            });
          }}
          onDecline={() => {
            setDetailsModal({ isOpen: false, reqNo: "" });
            setDeclineModal({ isOpen: true, reqNo: detailsModal.reqNo });
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

      <ApprovalsLayout
        filters={filters}
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
