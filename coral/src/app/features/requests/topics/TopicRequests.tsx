import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import {
  deleteTopicRequest,
  getTopicRequests,
} from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/topics/components/TopicRequestsTable";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { useState } from "react";
import { Alert } from "@aivenio/aquarium";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { parseErrorMsg } from "src/services/mutation-utils";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import TopicDetailsModalContent from "src/app/features/components/TopicDetailsModalContent";
import { SearchTopicFilter } from "src/app/features/components/filters/SearchTopicFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";

function TopicRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, status, showOnlyMyRequests, requestType } =
    useFiltersContext();

  const [modals, setModals] = useState<{
    open: "DETAILS" | "DELETE" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });
  const [errorQuickActions, setErrorQuickActions] = useState("");

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: [
      "topicRequests",
      currentPage,
      environment,
      status,
      search,
      showOnlyMyRequests,
      requestType,
    ],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        search: search,
        env: environment,
        requestStatus: status,
        isMyRequest: showOnlyMyRequests,
        operationType: requestType !== "ALL" ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  const { mutate: deleteRequest, isLoading: deleteRequestIsLoading } =
    useMutation(deleteTopicRequest, {
      onSuccess: async () => {
        setErrorQuickActions("");
        // We need to refetch all requests to keep Table state in sync
        await queryClient.refetchQueries(["topicRequests"]);
      },
      onError(error: Error) {
        setErrorQuickActions(parseErrorMsg(error));
      },
      onSettled: closeModal,
    });

  function closeModal() {
    setModals({ open: "NONE", req_no: null });
  }

  const handleDetails = (topicId: number) => {
    setModals({ open: "DETAILS", req_no: topicId });
  };

  const handleDeleteRequest = (topicId: number) => {
    setModals({ open: "DELETE", req_no: topicId });
  };

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  const selectedRequest = data?.entries.find(
    (request) => request.topicid === modals.req_no
  );

  return (
    <>
      {modals.open === "DETAILS" && (
        <RequestDetailsModal
          onClose={closeModal}
          actions={{
            primary: {
              text: "Close",
              onClick: () => {
                if (modals.req_no === null) {
                  throw Error("req_no can't be null");
                }
                closeModal();
              },
            },
            secondary: {
              text: "Delete",
              onClick: () => {
                if (modals.req_no === null) {
                  throw Error("req_no can't be null");
                }
                handleDeleteRequest(modals.req_no);
              },
            },
          }}
          isLoading={false}
        >
          <TopicDetailsModalContent topicRequest={selectedRequest} />
        </RequestDetailsModal>
      )}
      {modals.open === "DELETE" && (
        <DeleteRequestDialog
          deleteRequest={() => {
            if (modals.req_no === null) {
              throw Error("req_no can't be null");
            } else {
              deleteRequest({ reqIds: [modals.req_no.toString()] });
            }
          }}
          isLoading={deleteRequestIsLoading || isFetching}
          cancel={closeModal}
        />
      )}
      {errorQuickActions && <Alert type="error">{errorQuickActions}</Alert>}
      <TableLayout
        filters={[
          <EnvironmentFilter
            key="environments"
            environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
          />,
          <StatusFilter key="request-status" />,
          <RequestTypeFilter key={"request-type"} />,
          <SearchTopicFilter key={"topic"} />,
          <MyRequestsFilter key={"isMyRequest"} />,
        ]}
        table={
          <TopicRequestsTable
            requests={data?.entries ?? []}
            onDetails={handleDetails}
            onDelete={handleDeleteRequest}
            ariaLabel={`Topic requests, page ${data?.currentPage ?? 0} of ${
              data?.totalPages ?? 0
            }`}
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
  element: <TopicRequests />,
});
