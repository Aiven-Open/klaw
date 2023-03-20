import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Flexbox,
  StatusChip,
} from "@aivenio/aquarium";
import infoIcon from "@aivenio/aquarium/dist/src/icons/infoSign";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import { AclRequest } from "src/domain/acl/acl-types";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import {
  RequestStatus,
  RequestOperationType,
} from "src/domain/requests/requests-types";

interface AclRequestTableRow {
  id: number;
  topicname: AclRequest["topicname"];
  environmentName: string;
  acl_ssl: string[];
  acl_ip: string[];
  aclType: AclRequest["aclType"];
  requesttimestring: string;
  deletable: AclRequest["deletable"];
  editable: AclRequest["editable"];
  requestStatus: RequestStatus;
  requestOperationType: RequestOperationType;
}

type AclRequestsTableProps = {
  requests: AclRequest[];
  onDetails: (reqNo: string) => void;
  onDelete: (reqNo: string) => void;
};

function AclRequestsTable({
  requests,
  onDetails,
  onDelete,
}: AclRequestsTableProps) {
  const columns: Array<DataTableColumn<AclRequestTableRow>> = [
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
    {
      type: "custom",
      field: "acl_ssl",
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ acl_ssl }: AclRequestTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {acl_ssl.map((ssl, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${ssl}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${ssl} `}
              />
            ))}
          </Flexbox>
        );
      },
    },
    {
      type: "custom",
      field: "acl_ip",
      headerName: "IP addresses",
      UNSAFE_render: ({ acl_ip }: AclRequestTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {acl_ip.map((ip, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${ip}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${ip} `}
              />
            ))}
          </Flexbox>
        );
      },
    },
    {
      type: "status",
      field: "aclType",
      headerName: "ACL type",
      status: ({ aclType }) => ({
        status: aclType === "CONSUMER" ? "success" : "info",
        text: aclType,
      }),
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
      action: ({ id }) => ({
        text: "View",
        icon: infoIcon,
        onClick: () => onDetails(String(id)),
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

  const rows: AclRequestTableRow[] = requests.map((request: AclRequest) => {
    return {
      id: Number(request.req_no),
      topicname: request.topicname,
      environmentName: request.environmentName || "",
      requestStatus: request.requestStatus || "ALL",
      requestOperationType: request.requestOperationType || "CREATE",
      acl_ssl: request.acl_ssl || [],
      acl_ip: request.acl_ip || [],
      aclType: request.aclType,
      requesttimestring: request.requesttimestring || "",
      deletable: request.deletable,
      editable: request.editable,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No ACL requests">
        No ACL request matched your criteria.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={"ACL requests"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { AclRequestsTable, type AclRequestsTableProps };
