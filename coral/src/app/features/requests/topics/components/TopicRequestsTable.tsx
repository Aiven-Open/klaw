import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
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

interface TopicRequestTableRow {
  id: TopicRequest["topicid"];
  topicname: TopicRequest["topicname"];
  environmentName: TopicRequest["environmentName"];
  requestStatus: TopicRequest["requestStatus"];
  requestOperationType: TopicRequest["requestOperationType"];
  teamname: TopicRequest["teamname"];
  requestor: TopicRequest["requestor"];
  requesttimestring: TopicRequest["requesttimestring"];
  deletable: TopicRequest["deletable"];
}

type TopicRequestsTableProps = {
  requests: TopicRequest[];
  onDetails: (topicId: number) => void;
  onDelete: (topicId: number) => void;
  ariaLabel: string;
};

function TopicRequestsTable({
  requests,
  onDetails,
  onDelete,
  ariaLabel,
}: TopicRequestsTableProps) {
  const columns: Array<DataTableColumn<TopicRequestTableRow>> = [
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
      type: "custom",
      headerName: "Owned by",
      UNSAFE_render: ({ teamname }: TopicRequestTableRow) => {
        return <Link to={"/configuration/teams"}>{teamname}</Link>;
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
    {
      type: "custom",
      headerName: "Requested by",
      UNSAFE_render: ({ requestor }: TopicRequestTableRow) => {
        return <Link to={"/configuration/users"}>{requestor}</Link>;
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
      action: ({ id, topicname }) => ({
        text: "View",
        icon: infoIcon,
        "aria-label": `View topic request for ${topicname}`,
        onClick: () => onDetails(id),
      }),
    },
    {
      type: "action",
      headerName: "Delete",
      headerInvisible: true,
      width: 30,
      action: ({ id, deletable, topicname }) => ({
        text: "Delete",
        icon: deleteIcon,
        onClick: () => onDelete(id),
        "aria-label": `Delete topic request for ${topicname}`,
        disabled: !deletable,
      }),
    },
  ];

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
      deletable: request.deletable,
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

export { TopicRequestsTable, type TopicRequestsTableProps };
