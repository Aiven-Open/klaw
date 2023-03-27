import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import {
  deleteTopicRequest,
  getTopicRequests,
} from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/topics/components/TopicRequestsTable";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import TopicFilter from "src/app/features/components/filters/TopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { useState } from "react";
import { Alert } from "@aivenio/aquarium";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { parseErrorMsg } from "src/services/mutation-utils";

const defaultStatus = "ALL";

function TopicRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { topic, environment, status, showOnlyMyRequests } = useFiltersValues();

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
    ],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        // search is not yet implemented as a param to getTopicRequests
        // search: topic,
        env: environment,
        requestStatus: status,
        isMyRequest: showOnlyMyRequests,
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

  return (
    <>
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
          <StatusFilter key="request-status" defaultStatus={defaultStatus} />,
          <TopicFilter key={"topic"} />,
          <MyRequestsFilter key={"isMyRequest"} />,
        ]}
        table={
          <TopicRequestsTable
            requests={data?.entries ?? []}
            onDetails={() => null}
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
