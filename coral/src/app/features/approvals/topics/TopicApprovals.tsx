import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import DetailsModalContent from "src/app/features/approvals/topics/components/DetailsModalContent";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import useTableFilters from "src/app/features/approvals/topics/hooks/useTableFilters";
import { getTopicRequestsForApprover } from "src/domain/topic/topic-api";

function TopicApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const [detailsModal, setDetailsModal] = useState<{
    isOpen: boolean;
    topicId: number | null;
  }>({
    isOpen: false,
    topicId: null,
  });

  const { environment, status, team, topic, filters } = useTableFilters();

  const {
    data: topicRequests,
    isLoading: topicRequestsLoading,
    isError: topicRequestsIsError,
    error: topicRequestsError,
  } = useQuery({
    queryKey: [
      "topicRequestsForApprover",
      currentPage,
      environment,
      status,
      team,
      topic,
    ],
    queryFn: () =>
      getTopicRequestsForApprover({
        pageNo: String(currentPage),
        env: environment,
        requestStatus: status,
        teamId: team === "ALL" ? undefined : Number(team),
        search: topic,
      }),
    keepPreviousData: true,
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const table = (
    <TopicApprovalsTable
      requests={topicRequests?.entries || []}
      setDetailsModal={setDetailsModal}
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
    (request) => request.topicid === Number(detailsModal.topicId)
  );
  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, topicId: null })}
          onApprove={() => {
            alert("approve");
          }}
          onReject={() => {
            alert("reject");
          }}
          isLoading={false}
        >
          <DetailsModalContent topicRequest={selectedTopicRequest} />
        </RequestDetailsModal>
      )}
      <ApprovalsLayout
        filters={filters}
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
