import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import DetailsModalContent from "src/app/features/approvals/topics/components/DetailsModalContent";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TeamFilter from "src/app/features/components/table-filters/TeamFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { RequestStatus } from "src/domain/requests/requests-types";
import {
  approveTopicRequest,
  declineTopicRequest,
  getTopicRequestsForApprover,
} from "src/domain/topic/topic-api";
import { HTTPError } from "src/services/api";

function TopicApprovals() {
  const queryClient = useQueryClient();

  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  // This logic is what should be extracted in a useFilters hook?
  const currentTeam = searchParams.get("team") ?? "ALL";
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

  const [errorMessage, setErrorMessage] = useState("");

  const handleChangePage = (activePage: number) => {
    searchParams.set("page", activePage.toString());
    setSearchParams(searchParams);
  };

  const {
    data: topicRequests,
    isLoading: topicRequestsLoading,
    isError: topicRequestsIsError,
    error: topicRequestsError,
  } = useQuery({
    queryKey: [
      "topicRequestsForApprover",
      currentPage,
      currentEnv,
      currentStatus,
      currentTeam,
      currentTopic,
    ],
    queryFn: () =>
      getTopicRequestsForApprover({
        pageNo: String(currentPage),
        env: currentEnv,
        requestStatus: currentStatus,
        teamId: currentTeam === "ALL" ? undefined : Number(currentTeam),
        search: currentTopic,
      }),
    keepPreviousData: true,
  });

  const {
    isLoading: approveIsLoading,
    mutate: approveRequest,
    variables: approveVariables,
  } = useMutation({
    mutationFn: approveTopicRequest,
    onSuccess: (responses) => {
      // This mutation is used on a single request, so we always want the first response in the array
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
      if (
        topicRequests?.entries.length === 1 &&
        topicRequests?.currentPage > 1
      ) {
        return handleChangePage(currentPage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["topicRequestsForApprover"]);
    },
    onError: (error: HTTPError) => {
      const errorMessage = Array.isArray(error.data)
        ? error.data[0].message || error.data[0].result
        : "Unexpected error";

      setErrorMessage(errorMessage);
    },
  });

  const {
    isLoading: declineIsLoading,
    mutate: declineRequest,
    variables: declineVariables,
  } = useMutation({
    mutationFn: declineTopicRequest,
    onSuccess: (responses) => {
      // This mutation is used on a single request, so we always want the first response in the array
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
      if (
        topicRequests?.entries.length === 1 &&
        topicRequests?.currentPage > 1
      ) {
        return handleChangePage(currentPage - 1);
      }

      // We need to refetch all aclrequests queries to keep Table state in sync
      queryClient.refetchQueries(["topicRequestsForApprover"]);
    },
    onError: (error: HTTPError) => {
      const errorMessage = Array.isArray(error.data)
        ? error.data[0].message || error.data[0].result
        : "Unexpected error";

      setErrorMessage(errorMessage);
    },
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  function handleViewRequest(reqNo: number): void {
    setDetailsModal({ isOpen: true, reqNo });
  }

  function handleApproveRequest(reqNo: number): void {
    approveRequest({
      requestEntityType: "TOPIC",
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
    <TopicApprovalsTable
      requests={topicRequests?.entries || []}
      actionsDisabled={approveIsLoading || declineIsLoading}
      onDetails={handleViewRequest}
      onApprove={handleApproveRequest}
      onDecline={handleDeclineRequest}
      isBeingDeclined={handleIsBeingDeclined}
      isBeingApproved={handleIsBeingApproved}
    />
  );
  const pagination =
    topicRequests?.totalPages && topicRequests.totalPages > 1 ? (
      <Pagination
        activePage={topicRequests.currentPage}
        totalPages={topicRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  const selectedTopicRequest = topicRequests?.entries.find(
    (request) => request.topicid === Number(detailsModal.reqNo)
  );
  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: null })}
          actions={{
            primary: {
              text: "Approve",
              onClick: () => {
                if (detailsModal.reqNo === null) {
                  setErrorMessage("reqNo is null, it should be a number");
                  return;
                }
                approveRequest({
                  requestEntityType: "TOPIC",
                  reqIds: [String(detailsModal.reqNo)],
                });
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
          isLoading={approveIsLoading || declineIsLoading}
          disabledActions={
            selectedTopicRequest?.requestStatus !== "CREATED" ||
            approveIsLoading ||
            declineIsLoading
          }
        >
          <DetailsModalContent topicRequest={selectedTopicRequest} />
        </RequestDetailsModal>
      )}
      {declineModal.isOpen && (
        <RequestDeclineModal
          onClose={() => setDeclineModal({ isOpen: false, reqNo: null })}
          onCancel={() => setDeclineModal({ isOpen: false, reqNo: null })}
          onSubmit={(message: string) => {
            if (declineModal.reqNo === null) {
              setErrorMessage("reqNo is null, it should be a number");
              return;
            }
            declineRequest({
              requestEntityType: "TOPIC",
              reqIds: [String(declineModal.reqNo)],
              reason: message,
            });
          }}
          isLoading={declineIsLoading || approveIsLoading}
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
          <TeamFilter key={"team"} />,
          <TopicFilter key={"topic"} />,
        ]}
        table={table}
        pagination={pagination}
        isLoading={topicRequestsLoading}
        isErrorLoading={topicRequestsIsError}
        errorMessage={topicRequestsError}
      />
    </>
  );
}

export default TopicApprovals;
