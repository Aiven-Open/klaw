import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";
import { SchemaRequestDetails } from "src/app/features/components/SchemaRequestDetails";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/table-filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { SchemaRequestTable } from "src/app/features/requests/schemas/components/SchemaRequestTable";
import { RequestStatus } from "src/domain/requests/requests-types";
import {
  getSchemaRequests,
  deleteSchemaRequest,
} from "src/domain/schema-request";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import { parseErrorMsg } from "src/services/mutation-utils";
import { Alert } from "@aivenio/aquarium";

const defaultStatus = "ALL";

function SchemaRequests() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? undefined;
  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? defaultStatus;
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true" ? true : undefined;

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
  } = useQuery({
    queryKey: [
      "getSchemaRequests",
      currentPage,
      currentEnvironment,
      currentStatus,
      currentTopic,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getSchemaRequests({
        pageNo: String(currentPage),
        env: currentEnvironment,
        requestStatus: currentStatus,
        topic: currentTopic,
        isMyRequest: showOnlyMyRequests,
      }),
    keepPreviousData: true,
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { mutate: deleteRequest, isLoading: deleteRequestIsLoading } =
    useMutation(deleteSchemaRequest, {
      onSuccess: (responses) => {
        // @TODO follow up ticket #707
        // (for all approval and request tables)
        const response = responses[0];
        if (response.result !== "success") {
          //@TODO error handling
          setErrorQuickActions(
            response.message || response.result || "Unexpected error"
          );
        } else {
          setErrorQuickActions("");
          // If declined request is last in the page, go back to previous page
          // This avoids staying on a non-existent page of entries, which makes the table bug hard
          // With pagination being 0 of 0, and clicking Previous button sets active page at -1
          // We also do not need to invalidate the query, as the activePage does not exist any more
          // And there is no need to update anything on it
          if (
            schemaRequests?.entries.length === 1 &&
            schemaRequests?.currentPage > 1
          ) {
            return setCurrentPage(schemaRequests?.currentPage - 1);
          }

          // We need to refetch all requests to keep Table state in sync
          queryClient.refetchQueries(["getSchemaRequests"]);
        }
      },
      onError(error: Error) {
        setErrorQuickActions(parseErrorMsg(error));
      },
      onSettled() {
        closeModal();
      },
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
