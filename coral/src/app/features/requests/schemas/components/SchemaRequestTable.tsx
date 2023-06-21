import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { SchemaRequest } from "src/domain/schema-request";

interface SchemaRequestTableRow {
  deletable: SchemaRequest["deletable"];
  environment: SchemaRequest["environmentName"];
  id: SchemaRequest["req_no"];
  requestStatus: SchemaRequest["requestStatus"];
  requestType: SchemaRequest["requestOperationType"];
  requestedBy: SchemaRequest["requestor"];
  requestedOn: SchemaRequest["requesttimestring"];
  schemaversion: SchemaRequest["schemaversion"];
  topic: SchemaRequest["topicname"];
}

type SchemaRequestTableProps = {
  requests: SchemaRequest[];
  showDetails: (req_no: number) => void;
  showDeleteDialog: (req_no: number) => void;
  ariaLabel: string;
};

function SchemaRequestTable({
  requests,
  showDetails,
  showDeleteDialog,
  ariaLabel,
}: SchemaRequestTableProps) {
  const columns: Array<DataTableColumn<SchemaRequestTableRow>> = [
    { type: "text", field: "topic", headerName: "Topic" },
    {
      type: "status",
      headerName: "Environment",
      status: ({ environment }) => ({
        status: "neutral",
        text: environment,
      }),
    },
    {
      type: "status",
      headerName: "Status",
      status: ({ requestStatus }) => {
        return {
          status: requestStatusChipStatusMap[requestStatus],
          text: requestStatusNameMap[requestStatus],
        };
      },
    },
    {
      type: "status",
      headerName: "Request type",
      status: ({ requestType }) => {
        return {
          status: requestOperationTypeChipStatusMap[requestType],
          text: requestOperationTypeNameMap[requestType],
        };
      },
    },
    { type: "text", field: "requestedBy", headerName: "Requested by" },
    {
      type: "text",
      field: "requestedOn",
      headerName: "Requested on",
      formatter: (value) => {
        return `${value}${"\u00A0"}UTC`;
      },
    },
    {
      type: "action",
      headerName: "Details",
      headerInvisible: true,
      width: 30,
      action: ({ id, topic }) => ({
        text: "View",
        icon: infoIcon,
        onClick: () => showDetails(id),
        "aria-label": `View schema request for ${topic}`,
      }),
    },
    {
      type: "action",
      headerName: "Delete",
      headerInvisible: true,
      width: 30,
      action: ({ id, deletable, topic }) => ({
        text: "Delete",
        icon: deleteIcon,
        onClick: () => showDeleteDialog(id),
        disabled: !deletable,
        "aria-label": `Delete schema request for ${topic}`,
      }),
    },
  ];

  const rows: SchemaRequestTableRow[] = requests.map(
    (request: SchemaRequest): SchemaRequestTableRow => {
      return {
        deletable: request.deletable,
        environment: request.environmentName,
        id: request.req_no,
        requestStatus: request.requestStatus,
        requestType: request.requestOperationType,
        requestedBy: request.requestor,
        requestedOn: request.requesttimestring,
        schemaversion: request.schemaversion,
        topic: request.topicname,
      };
    }
  );

  if (!rows.length) {
    return (
      <EmptyState title="No Schema requests">
        No Schema request matched your criteria.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { SchemaRequestTable, type SchemaRequestTableProps };
