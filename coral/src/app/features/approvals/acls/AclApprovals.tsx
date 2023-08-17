import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import AclApprovalsTable from "src/app/features/approvals/acls/components/AclApprovalsTable";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import AclDetailsModalContent from "src/app/features/components/AclDetailsModalContent";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
import { AclRequestsForApprover } from "src/domain/acl/acl-types";
import { parseErrorMsg } from "src/services/mutation-utils";

const defaultType = "ALL";

function AclApprovals() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { aclType, environment, status, search, requestType } =
    useFiltersContext();

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
    queryKey: [
      "aclRequests",
      currentPage,
      aclType,
      environment,
      status,
      search,
      requestType,
    ],
    queryFn: () =>
      getAclRequestsForApprover({
        pageNo: String(currentPage),
        env: environment,
        requestStatus: status,
        aclType,
        search: search,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  const {
    isLoading: approveIsLoading,
    mutate: approveRequest,
    variables: approveVariables,
  } = useMutation({
    mutationFn: approveAclRequest,
    onSuccess: () => {
      setErrorMessage("");

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        handleChangePage(currentPage - 1);
        return;
      }
      // Refetch to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
    onSettled: () => {
      setDetailsModal({ isOpen: false, reqNo: null });
    },
  });

  const {
    isLoading: declineIsLoading,
    mutate: declineRequest,
    variables: declineVariables,
  } = useMutation({
    mutationFn: declineAclRequest,
    onSuccess: () => {
      setErrorMessage("");

      // Refetch to update the tag number in the tabs
      queryClient.refetchQueries(["getRequestsWaitingForApproval"]);

      // If approved request is last in the page, go back to previous page
      // This avoids staying on a non-existent page of entries, which makes the table bug hard
      // With pagination being 0 of 0, and clicking Previous button sets active page at -1
      // We also do not need to invalidate the query, as the activePage does not exist any more
      // And there is no need to update anything on it
      if (data?.entries.length === 1 && data?.currentPage > 1) {
        handleChangePage(currentPage - 1);
        return;
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["aclRequests"]);
    },
    onError: (error: Error) => {
      setErrorMessage(parseErrorMsg(error));
    },
    onSettled: () => {
      setDeclineModal({ isOpen: false, reqNo: null });
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
      {errorMessage !== "" && <Alert type="error">{errorMessage}</Alert>}

      <TableLayout
        filters={[
          <EnvironmentFilter
            key={"environment"}
            environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
          />,
          <StatusFilter key={"status"} />,
          <RequestTypeFilter key={"requestType"} />,
          <AclTypeFilter key={"aclType"} />,
          <SearchTopicFilter key={"topic"} />,
        ]}
        table={
          <AclApprovalsTable
            aclRequests={data?.entries ?? []}
            actionsDisabled={approveIsLoading || declineIsLoading}
            onDetails={handleViewRequest}
            onApprove={handleApproveRequest}
            onDecline={handleDeclineRequest}
            isBeingDeclined={handleIsBeingDeclined}
            isBeingApproved={handleIsBeingApproved}
            ariaLabel={`ACL approval requests, page ${
              data?.currentPage ?? 0
            } of ${data?.totalPages ?? 0}`}
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

export default withFiltersContext({
  defaultValues: { status: "CREATED" },
  element: <AclApprovals />,
});
