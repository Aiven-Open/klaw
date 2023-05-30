import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { ConnectorRequestsTable } from "src/app/features/requests/connectors/components/ConnectorRequestsTable";
import {
  deleteConnectorRequest,
  getConnectorRequests,
} from "src/domain/connector";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { SearchConnectorFilter } from "src/app/features/components/filters/SearchConnectorFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { Alert } from "@aivenio/aquarium";
import { useState } from "react";
import { parseErrorMsg } from "src/services/mutation-utils";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import { ConnectorRequestDetails } from "src/app/features/components/ConnectorDetailsModalContent";

function ConnectorRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, status, showOnlyMyRequests, requestType } =
    useFiltersContext();

  const [modals, setModals] = useState<{
    open: "DETAILS" | "DELETE" | "NONE";
    connectorId: number | null;
  }>({ open: "NONE", connectorId: null });
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
    setModals({ open: "NONE", connectorId: null });
  }

  const handleDetails = (connectorId: number) => {
    setModals({ open: "DETAILS", connectorId });
  };

  const handleDeleteRequest = (connectorId: number) => {
    setModals({ open: "DELETE", connectorId });
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
        operationType: requestType !== "ALL" ? requestType : undefined,
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

  const selectedRequest = data?.entries.find(
    (request) => request.connectorId === modals.connectorId
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
                if (modals.connectorId === null) {
                  throw Error("connectorId can't be null");
                }
                closeModal();
              },
            },
            secondary: {
              text: "Delete",
              onClick: () => {
                if (modals.connectorId === null) {
                  throw Error("connectorId can't be null");
                }
                handleDeleteRequest(modals.connectorId);
              },
            },
          }}
          isLoading={false}
        >
          <ConnectorRequestDetails request={selectedRequest} />
        </RequestDetailsModal>
      )}
      {modals.open === "DELETE" && (
        <DeleteRequestDialog
          deleteRequest={() => {
            if (modals.connectorId === null) {
              throw Error("connectorId can't be null");
            } else {
              deleteRequest({ reqIds: [modals.connectorId.toString()] });
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
          <EnvironmentFilter
            key="environment"
            environmentEndpoint="getAllEnvironmentsForConnector"
          />,
          <StatusFilter key="request-status" />,
          <RequestTypeFilter key={"request-type"} />,
          <SearchConnectorFilter key="connector" />,
          <MyRequestsFilter key={"isMyRequest"} />,
        ]}
        table={
          <ConnectorRequestsTable
            ariaLabel="Connector requests"
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

export default withFiltersContext({
  defaultValues: { status: "ALL", requestType: "ALL" },
  element: <ConnectorRequests />,
});
