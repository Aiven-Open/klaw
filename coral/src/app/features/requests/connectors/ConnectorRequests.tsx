import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { ConnectorRequestsTable } from "src/app/features/requests/connectors/components/ConnectorRequestsTable";
import {
  deleteConnectorRequest,
  getConnectorRequests,
} from "src/domain/connector";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import SearchFilter from "src/app/features/components/filters/SearchFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { Alert } from "@aivenio/aquarium";
import { useState } from "react";
import { parseErrorMsg } from "src/services/mutation-utils";

const defaultStatus = "ALL";
const defaultType = "ALL";

function ConnectorRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, status, showOnlyMyRequests, requestType } =
    useFiltersValues();

  const [modals, setModals] = useState<{
    open: "DETAILS" | "DELETE" | "NONE";
    req_no: number | null;
  }>({ open: "NONE", req_no: null });
  const [errorQuickActions, setErrorQuickActions] = useState("");

  const { mutate: deleteRequest, isLoading: deleteRequestIsLoading } =
    useMutation(deleteConnectorRequest, {
      onSuccess: async () => {
        setErrorQuickActions("");
        // We need to refetch all requests to keep Table state in sync
        await queryClient.refetchQueries(["connectorRequests"]);
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

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: [
      "connectorRequests",
      currentPage,
      search,
      environment,
      showOnlyMyRequests,
      status,
      requestType,
    ],
    queryFn: () =>
      getConnectorRequests({
        pageNo: String(currentPage),
        isMyRequest: showOnlyMyRequests,
        env: environment,
        search,
        requestStatus: status,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

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
          <SearchFilter key="connector" />,
          <EnvironmentFilter
            key="environment"
            environmentEndpoint="getSyncConnectorsEnvironments"
          />,
          <StatusFilter key="request-status" defaultStatus={defaultStatus} />,
          <RequestTypeFilter key={"request-type"} />,
          <MyRequestsFilter key={"isMyRequest"} />,
        ]}
        table={
          <ConnectorRequestsTable
            ariaLabel="Connector requests"
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

export { ConnectorRequests };
