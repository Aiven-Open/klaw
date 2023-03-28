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
import { objectHasProperty } from "src/services/type-utils";
import { parseErrorMsg } from "src/services/mutation-utils";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import TopicDetailsModalContent from "src/app/features/components/TopicDetailsModalContent";
import TopicFilter from "src/app/features/components/filters/TopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";

const defaultStatus = "ALL";
const defaultType = "ALL";

function TopicRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { topic, environment, status, showOnlyMyRequests, requestType } =
    useFiltersValues();

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
      topic,
      showOnlyMyRequests,
      requestType,
    ],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        // search is not yet implemented as a param to getTopicRequests
        // search: topic,
        env: environment,
        requestStatus: status,
        isMyRequest: showOnlyMyRequests,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  const { mutate: deleteRequest, isLoading: deleteRequestIsLoading } =
    useMutation(deleteTopicRequest, {
      onSuccess: (responses) => {
        // @TODO follow up ticket #707
        // (for all approval and request tables)
        const response = responses[0];
        const responseIsAHiddenError = response?.result !== "success";
        if (responseIsAHiddenError) {
          throw new Error(response?.message || response?.result);
        }
        setErrorQuickActions("");
        // We need to refetch all requests to keep Table state in sync
        queryClient.refetchQueries(["topicRequests"]).then(() => {
          // only close the modal when data in background is updated
          closeModal();
        });
      },
      onError(error: Error) {
        let errorMessage: string;
        // if error comes from our api, it has a `data` property
        // parseErrorMsg makes sure to get the right message
        // OR set a generic error message
        if (objectHasProperty(error, "data")) {
          errorMessage = parseErrorMsg(error);
        } else {
          errorMessage = error.message;
        }

        setErrorQuickActions(errorMessage);
        closeModal();
      },
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
      {errorQuickActions && (
        <div role="alert">
          <Alert type="error">{errorQuickActions}</Alert>
        </div>
      )}
      <TableLayout
        filters={[
          <EnvironmentFilter key="environments" />,
          <RequestTypeFilter key={"request-type"} />,
          <StatusFilter key="request-status" defaultStatus={defaultStatus} />,
          <TopicFilter key={"topic"} />,
          <MyRequestsFilter key={"isMyRequest"} />,
        ]}
        table={
          <TopicRequestsTable
            requests={data?.entries ?? []}
            onDetails={handleDetails}
            onDelete={handleDeleteRequest}
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

export { TopicRequests };
