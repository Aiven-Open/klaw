import { Alert } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/components/SchemaRequestDetails";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import TopicFilter from "src/app/features/components/filters/TopicFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { SchemaRequestTable } from "src/app/features/requests/schemas/components/SchemaRequestTable";
import {
  deleteSchemaRequest,
  getSchemaRequests,
} from "src/domain/schema-request";
import { parseErrorMsg } from "src/services/mutation-utils";

const defaultStatus = "ALL";
const defaultType = "ALL";

function SchemaRequests() {
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

  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
    isFetching,
  } = useQuery({
    queryKey: [
      "getSchemaRequests",
      currentPage,
      environment,
      status,
      topic,
      showOnlyMyRequests,
      requestType,
    ],
    queryFn: () =>
      getSchemaRequests({
        pageNo: String(currentPage),
        env: environment,
        requestStatus: status !== defaultStatus ? status : undefined,
        topic,
        isMyRequest: showOnlyMyRequests,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { mutate: deleteRequest, isLoading: deleteRequestIsLoading } =
    useMutation(deleteSchemaRequest, {
      onSuccess: async () => {
        setErrorQuickActions("");
        // We need to refetch all requests to keep Table state in sync
        await queryClient.refetchQueries(["getSchemaRequests"]);
      },
      onError(error: Error) {
        setErrorQuickActions(parseErrorMsg(error));
      },
      onSettled: closeModal,
    });

  function closeModal() {
    setModals({ open: "NONE", req_no: null });
  }

  const openDetailsModal = (req_no: number) => {
    setModals({ open: "DETAILS", req_no });
  };

  const openDeleteModal = (req_no: number) => {
    setModals({ open: "DELETE", req_no });
  };

  function setCurrentPage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }

  const pagination =
    schemaRequests?.totalPages && schemaRequests.totalPages > 1 ? (
      <Pagination
        activePage={schemaRequests.currentPage}
        totalPages={schemaRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

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
                openDeleteModal(modals.req_no);
              },
            },
          }}
          isLoading={false}
        >
          <SchemaRequestDetails
            request={schemaRequests?.entries.find(
              (request) => request.req_no === modals.req_no
            )}
          />
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
          <EnvironmentFilter
            key={"environments"}
            isSchemaRegistryEnvironments
          />,
          <StatusFilter key={"request-status"} defaultStatus={defaultStatus} />,
          <RequestTypeFilter key={"request-type"} />,
          <TopicFilter key={"topic"} />,
          <MyRequestsFilter key={"show-only-my-requests"} />,
        ]}
        table={
          <SchemaRequestTable
            requests={schemaRequests?.entries || []}
            showDetails={openDetailsModal}
            showDeleteDialog={openDeleteModal}
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

export { SchemaRequests };
