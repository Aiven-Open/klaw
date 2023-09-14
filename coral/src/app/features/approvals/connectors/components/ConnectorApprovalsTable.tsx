import {
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
} from "@aivenio/aquarium";
import infoSign from "@aivenio/aquarium/icons/infoSign";
import tickCircle from "@aivenio/aquarium/icons/tickCircle";
import deleteIcon from "@aivenio/aquarium/icons/delete";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import loadingIcon from "@aivenio/aquarium/icons/loading";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { ConnectorRequest } from "src/domain/connector";
import { Link } from "react-router-dom";
import link from "@aivenio/aquarium/icons/link";

interface ConnectorRequestTableData {
  id: ConnectorRequest["connectorId"];
  connectorName: ConnectorRequest["connectorName"];
  environmentName: ConnectorRequest["environmentName"];
  requestor: ConnectorRequest["requestor"];
  requesttimestring: ConnectorRequest["requesttimestring"];
  requestStatus: ConnectorRequest["requestStatus"];
  requestOperationType: ConnectorRequest["requestOperationType"];
}

type ConnectorApprovalsTableProps = {
  requests: ConnectorRequest[];
  ariaLabel: string;
  actionsDisabled?: boolean;
  onDetails: (req_no: number) => void;
  onApprove: (req_no: number) => void;
  onDecline: (req_no: number) => void;
  isBeingApproved: (req_no: number) => boolean;
  isBeingDeclined: (req_no: number) => boolean;
};

function ConnectorApprovalsTable({
  requests,
  ariaLabel,
  actionsDisabled = false,
  onDetails,
  onApprove,
  onDecline,
  isBeingApproved,
  isBeingDeclined,
}: ConnectorApprovalsTableProps) {
  const columns: Array<DataTableColumn<ConnectorRequestTableData>> = [
    {
      type: "custom",
      headerName: "Connector name",
      UNSAFE_render: ({
        connectorName,
        requestOperationType,
      }: ConnectorRequestTableData) => {
        if (requestOperationType !== "CREATE") {
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
      status: ({ environmentName }) => {
        return {
          status: "neutral",
          text: environmentName,
        };
      },
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
      action: (request) => ({
        onClick: () => onDetails(request.id),
        icon: infoSign,
        text: "View",
        "aria-label": `View Kafka connector request for ${request.connectorName}`,
      }),
    },
    {
      type: "action",
      headerName: "Approve",
      headerInvisible: true,
      width: 30,
      action: (request) => {
        const approveInProgress = isBeingApproved(request.id);
        const declineInProgress = isBeingDeclined(request.id);
        return {
          onClick: () => onApprove(request.id),
          text: "Approve",
          "aria-label": `Approve Kafka connector request for ${request.connectorName}`,
          disabled:
            approveInProgress ||
            declineInProgress ||
            actionsDisabled ||
            request.requestStatus !== "CREATED",
          icon: approveInProgress ? loadingIcon : tickCircle,
          loading: approveInProgress,
        };
      },
    },
    {
      type: "action",
      headerName: "Decline",
      headerInvisible: true,
      width: 30,
      action: (request) => {
        const approveInProgress = isBeingApproved(request.id);
        const declineInProgress = isBeingDeclined(request.id);
        return {
          onClick: () => onDecline(request.id),
          text: "Decline",
          "aria-label": `Decline Kafka connector request for ${request.connectorName}`,
          disabled:
            approveInProgress ||
            declineInProgress ||
            actionsDisabled ||
            request.requestStatus !== "CREATED",
          icon: declineInProgress ? loadingIcon : deleteIcon,
          loading: declineInProgress,
        };
      },
    },
  ];
  const rows: ConnectorRequestTableData[] = requests.map(
    ({
      connectorId,
      connectorName,
      environmentName,
      requestor,
      requesttimestring,
      requestStatus,
      requestOperationType,
    }: ConnectorRequest) => {
      return {
        id: connectorId,
        connectorName,
        environmentName,
        requestor,
        requesttimestring,
        requestStatus,
        requestOperationType,
      };
    }
  );

  if (!rows.length) {
    return (
      <EmptyState title="No Kafka connector requests">
        No Kafka connector request matched your criteria.
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

export default ConnectorApprovalsTable;
