import {
  DataTable,
  DataTableColumn,
  GhostButton,
  Icon,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import {
  MutationFunction,
  QueryKey,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import { useState } from "react";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";
import RequestRejectModal from "src/app/features/approvals/components/RequestRejectModal";

interface BaseTableData {
  // id should be req_no of request
  id: number;
}

interface RequestApprovalTableProps<TableData extends BaseTableData> {
  name: string;
  columns: Array<DataTableColumn<TableData>>;
  rows: TableData[];
  dataQueryKey: QueryKey;
  approveFn: MutationFunction<unknown, { req_no: string }>;
  rejectFn: MutationFunction<
    unknown,
    { req_no: string; reasonForDecline: string }
  >;
}

function RequestApprovalTable<TableData extends BaseTableData>({
  name,
  columns,
  rows,
  dataQueryKey,
  approveFn,
  rejectFn,
}: RequestApprovalTableProps<TableData>) {
  const queryClient = useQueryClient();

  const [detailsModal, setDetailsModal] = useState({
    isOpen: false,
    reqNo: "",
  });
  const [rejectModal, setRejectModal] = useState({ isOpen: false, reqNo: "" });

  const { isLoading: approveIsLoading, mutate: approveRequest } = useMutation({
    mutationFn: approveFn,
    onSuccess: () => {
      setDetailsModal({ isOpen: false, reqNo: "" });
      // We need to invalidate the query populating the table to reflect the change
      queryClient.invalidateQueries(dataQueryKey);
    },
  });

  const { isLoading: rejectIsLoading, mutate: rejectRequest } = useMutation({
    mutationFn: rejectFn,
    onSuccess: () => {
      setRejectModal({ isOpen: false, reqNo: "" });
      // We need to invalidate the query populating the table to reflect the change
      queryClient.invalidateQueries(dataQueryKey);
    },
  });

  const columnsWithActions: Array<DataTableColumn<TableData>> = [
    ...columns,
    {
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id }: TableData) => {
        return (
          <GhostButton
            icon={infoSign}
            onClick={() => setDetailsModal({ isOpen: true, reqNo: String(id) })}
            title={"View request details"}
            dense
          >
            View details
          </GhostButton>
        );
      },
    },
    {
      width: 30,
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id }: TableData) => {
        const [isLoading, setIsLoading] = useState(false);
        return isLoading ? (
          <Icon icon={loadingIcon} />
        ) : (
          <GhostButton
            onClick={() => {
              setIsLoading(true);
              return approveRequest({ req_no: String(id) });
            }}
            title={"Approve request"}
          >
            <Icon color="grey-70" icon={tickCircle} />
          </GhostButton>
        );
      },
    },
    {
      width: 30,
      // Not having a headerName triggers React error:
      // Warning: Encountered two children with the same key, ``.
      headerName: "",
      type: "custom",
      UNSAFE_render: ({ id }: TableData) => {
        return (
          <GhostButton
            onClick={() => setRejectModal({ isOpen: true, reqNo: String(id) })}
            title={"Reject request"}
          >
            <Icon color="grey-70" icon={deleteIcon} />
          </GhostButton>
        );
      },
    },
  ];

  return (
    <>
      {detailsModal.isOpen && (
        <RequestDetailsModal
          onClose={() => setDetailsModal({ isOpen: false, reqNo: "" })}
          onApprove={() => {
            approveRequest({ req_no: detailsModal.reqNo });
          }}
          onReject={() => {
            setDetailsModal({ isOpen: false, reqNo: "" });
            setRejectModal({ isOpen: true, reqNo: detailsModal.reqNo });
          }}
          isLoading={approveIsLoading}
        >
          <div>
            {JSON.stringify(
              rows.find((request) => request.id === Number(detailsModal.reqNo))
            )}
          </div>
        </RequestDetailsModal>
      )}
      {rejectModal.isOpen && (
        <RequestRejectModal
          onClose={() => setRejectModal({ isOpen: false, reqNo: "" })}
          onCancel={() =>
            setRejectModal({ isOpen: false, reqNo: rejectModal.reqNo })
          }
          onSubmit={(message: string) => {
            rejectRequest({
              req_no: rejectModal.reqNo,
              reasonForDecline: message,
            });
          }}
          isLoading={rejectIsLoading}
        />
      )}
      <DataTable
        ariaLabel={name}
        columns={columnsWithActions}
        rows={rows}
        noWrap={false}
      />
    </>
  );
}

export default RequestApprovalTable;
