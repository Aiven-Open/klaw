import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import { TopicRequest } from "src/domain/topic/topic-types";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { Link } from "react-router-dom";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

interface TopicRequestTableRow {
  id: TopicRequest["topicid"];
  topicname: TopicRequest["topicname"];
  environmentName: TopicRequest["environmentName"];
  requestStatus: TopicRequest["requestStatus"];
  requestOperationType: TopicRequest["requestOperationType"];
  teamname: TopicRequest["teamname"];
  requestor: TopicRequest["requestor"];
  requesttimestring: TopicRequest["requesttimestring"];
}

type TopicApprovalsTableProp = {
  requests: TopicRequest[];
  ariaLabel: string;
  actionsDisabled?: boolean;
  onDetails: (req_no: number) => void;
  onApprove: (req_no: number) => void;
  onDecline: (req_no: number) => void;
  isBeingApproved: (req_no: number) => boolean;
  isBeingDeclined: (req_no: number) => boolean;
};
function TopicApprovalsTable({
  requests,
  ariaLabel,
  actionsDisabled = false,
  onDetails,
  onApprove,
  onDecline,
  isBeingApproved,
  isBeingDeclined,
}: TopicApprovalsTableProp) {
  const {
    permissions: { approveDeclineTopics },
  } = useAuthContext();

  let columns: Array<DataTableColumn<TopicRequestTableRow>> = [
    { type: "text", field: "topicname", headerName: "Topic" },
    {
      type: "status",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
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
      status: ({ requestOperationType }) => {
        return {
          status: requestOperationTypeChipStatusMap[requestOperationType],
          text: requestOperationTypeNameMap[requestOperationType],
        };
      },
    },
    {
      type: "custom",
      headerName: "Requested by",
      UNSAFE_render: ({ requestor }: TopicRequestTableRow) => {
        return <Link to={"/configuration/users"}>{requestor}</Link>;
      },
    },
    {
      type: "custom",
      headerName: "Team",
      UNSAFE_render: ({ teamname }: TopicRequestTableRow) => {
        return <Link to={"/configuration/teams"}>{teamname}</Link>;
      },
    },
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
        "aria-label": `View topic request for ${request.topicname}`,
      }),
    },
  ];

  if (approveDeclineTopics) {
    columns = [
      ...columns,
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
            "aria-label": `Approve topic request for ${request.topicname}`,
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
            "aria-label": `Decline topic request for ${request.topicname}`,
            text: "Decline",
            disabled:
              declineInProgress ||
              approveInProgress ||
              actionsDisabled ||
              request.requestStatus !== "CREATED",
            icon: isBeingDeclined(request.id) ? loadingIcon : deleteIcon,
            loading: isBeingDeclined(request.id),
          };
        },
      },
    ];
  }

  const rows: TopicRequestTableRow[] = requests.map((request: TopicRequest) => {
    return {
      id: request.topicid,
      topicname: request.topicname,
      environmentName: request.environmentName,
      requestStatus: request.requestStatus,
      requestOperationType: request.requestOperationType,
      teamname: request.teamname,
      requestor: request.requestor,
      requesttimestring: request.requesttimestring,
    };
  });

  if (!rows.length) {
    return (
      <EmptyState title="No Topic requests">
        No Topic request matched your criteria.
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

export { TopicApprovalsTable };
