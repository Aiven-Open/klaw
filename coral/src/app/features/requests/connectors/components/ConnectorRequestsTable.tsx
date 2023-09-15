import {
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
} from "@aivenio/aquarium";
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
import { Link } from "react-router-dom";
import link from "@aivenio/aquarium/icons/link";
import { doesEntityRelatedToRequestExists } from "src/services/entity-exists/entity-related-to-request-exists";

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
    {
      type: "custom",
      headerName: "Name",
      UNSAFE_render: ({
        connectorName,
        requestStatus,
        requestOperationType,
      }: ConnectorRequestTableRow) => {
        if (
          doesEntityRelatedToRequestExists({
            requestStatus,
            requestOperationType,
          })
        ) {
          return (
            <Link to={`/connector/${connectorName}/overview`}>
              {connectorName} <InlineIcon icon={link} />
            </Link>
          );
        } else {
          return <>{connectorName}</>;
        }
      },
    },

    {
      type: "status",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
    { type: "text", field: "teamname", headerName: "Owned by" },
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
