import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import type { ConnectorRequest } from "src/domain/connector";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";

type ConnectorRequestTableRow = {
  id: ConnectorRequest["connectorId"];
  connectorName: ConnectorRequest["connectorName"];
  environmentName: ConnectorRequest["environmentName"];
  requestStatus: ConnectorRequest["requestStatus"];
  requestOperationType: ConnectorRequest["requestOperationType"];
  teamname: ConnectorRequest["teamname"];
  requestor: ConnectorRequest["requestor"];
  requesttimestring: ConnectorRequest["requesttimestring"];
  deletable: ConnectorRequest["deletable"];
};

type Props = {
  ariaLabel: string;
  requests: ConnectorRequest[];
  onDetails: (reqNo: number) => void;
  onDelete: (reqNo: number) => void;
};

function ConnectorRequestsTable({
  ariaLabel,
  requests,
  onDetails,
  onDelete,
}: Props) {
  const columns: Array<DataTableColumn<ConnectorRequestTableRow>> = [
    { type: "text", field: "connectorName", headerName: "Name" },
    {
      type: "status",
      field: "environmentName",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
    { type: "text", field: "teamname", headerName: "Owned by" },
    {
      type: "status",
      field: "requestStatus",
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
      field: "requestOperationType",
      headerName: "Request type",
      status: ({ requestOperationType }) => {
        return {
          status: requestOperationTypeChipStatusMap[requestOperationType],
          text: requestOperationTypeNameMap[requestOperationType],
        };
      },
    },
    { type: "text", field: "requestor", headerName: "Requested by" },
    {
      type: "text",
      field: "requesttimestring",
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
      action: ({ id, connectorName }) => ({
        text: "View",
        icon: infoIcon,
        "aria-label": `View connector request for ${connectorName}`,
        onClick: () => onDetails(id),
      }),
    },
    {
      type: "action",
      headerName: "Delete",
      headerInvisible: true,
      width: 30,
      action: ({ id, deletable, connectorName }) => ({
        text: "Delete",
        icon: deleteIcon,
        onClick: () => onDelete(id),
        "aria-label": `Delete connector request for ${connectorName}`,
        disabled: !deletable,
      }),
    },
  ];

  const rows: ConnectorRequestTableRow[] = requests.map(
    (request: ConnectorRequest) => {
      return {
        id: request.connectorId,
        connectorName: request.connectorName,
        environmentName: request.environmentName,
        requestStatus: request.requestStatus,
        requestOperationType: request.requestOperationType,
        teamname: request.teamname,
        requestor: request.requestor,
        requesttimestring: request.requesttimestring,
        deletable: request.deletable,
      };
    }
  );

  if (!rows.length) {
    return (
      <EmptyState title="No Connector requests">
        No Connector request matched your criteria.
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

export { ConnectorRequestsTable, type Props as ConnectorRequestsTableProps };
