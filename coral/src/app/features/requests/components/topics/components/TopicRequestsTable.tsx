import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import editIcon from "@aivenio/aquarium/dist/src/icons/edit";
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
  editable: TopicRequest["editable"];
}

type TopicRequestsTableProps = {
  requests: TopicRequest[];
  onDetails: (topicId: string) => void;
  onEdit: (topicId: string) => void;
  onDelete: (topicId: string) => void;
};

function TopicRequestsTable({
  requests,
  onDetails,
  onEdit,
  onDelete,
}: TopicRequestsTableProps) {
  const columns: Array<DataTableColumn<TopicRequestTableRow>> = [
    { type: "text", field: "topicname", headerName: "Topic" },
    {
      type: "status",
      field: "environmentName",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
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
      headerName: "Type",
      status: ({ requestOperationType }) => {
        return {
          status: requestOperationTypeChipStatusMap[requestOperationType],
          text: requestOperationTypeNameMap[requestOperationType],
        };
      },
    },
    { type: "text", field: "teamname", headerName: "Owned by" },
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
      action: ({ id }) => ({
        text: "View",
        icon: infoIcon,
        onClick: () => onDetails(String(id)),
      }),
    },
    {
      type: "action",
      headerName: "Edit",
      headerInvisible: true,
      width: 30,
      action: ({ id, editable }) => ({
        text: "Edit",
        icon: editIcon,
        onClick: () => onEdit(String(id)),
        disabled: !editable,
      }),
    },
    {
      type: "action",
      headerName: "Delete",
      headerInvisible: true,
      width: 30,
      action: ({ id, deletable }) => ({
        text: "Delete",
        icon: deleteIcon,
        onClick: () => onDelete(String(id)),
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
      editable: request.editable,
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
      ariaLabel={"Topic requests"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { TopicRequestsTable, type TopicRequestsTableProps };
