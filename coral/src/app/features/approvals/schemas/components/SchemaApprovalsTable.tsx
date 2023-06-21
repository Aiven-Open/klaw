import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { SchemaRequest } from "src/domain/schema-request";
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

interface SchemaRequestTableData {
  id: SchemaRequest["req_no"];
  topicname: SchemaRequest["topicname"];
  environmentName: SchemaRequest["environmentName"];
  requestor: SchemaRequest["requestor"];
  requesttimestring: SchemaRequest["requesttimestring"];
  requestStatus: SchemaRequest["requestStatus"];
  requestOperationType: SchemaRequest["requestOperationType"];
}

type SchemaApprovalsTableProps = {
  requests: SchemaRequest[];
  ariaLabel: string;
  actionsDisabled?: boolean;
  onDetails: (req_no: number) => void;
  onApprove: (req_no: number) => void;
  onDecline: (req_no: number) => void;
  isBeingApproved: (req_no: number) => boolean;
  isBeingDeclined: (req_no: number) => boolean;
};

function SchemaApprovalsTable({
  requests,
  ariaLabel,
  actionsDisabled = false,
  onDetails,
  onApprove,
  onDecline,
  isBeingApproved,
  isBeingDeclined,
}: SchemaApprovalsTableProps) {
  const columns: Array<DataTableColumn<SchemaRequestTableData>> = [
    { type: "text", field: "topicname", headerName: "Topic" },
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
        "aria-label": `View schema request for ${request.topicname}`,
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
          "aria-label": `Approve schema request for ${request.topicname}`,
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
          "aria-label": `Decline schema request for ${request.topicname}`,
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
  const rows: SchemaRequestTableData[] = requests.map(
    (request: SchemaRequest) => {
      return {
        id: request.req_no,
        topicname: request.topicname,
        environmentName: request.environmentName,
        requestor: request.requestor,
        requesttimestring: request.requesttimestring,
        requestStatus: request.requestStatus,
        requestOperationType: request.requestOperationType,
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

export default SchemaApprovalsTable;
