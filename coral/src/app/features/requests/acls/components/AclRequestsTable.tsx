import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
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
  onDetails: (req_no: number) => void;
  onDelete: (req_no: number) => void;
  ariaLabel: string;
};

function AclRequestsTable({
  requests,
  onDetails,
  onDelete,
  ariaLabel,
}: AclRequestsTableProps) {
  const columns: Array<DataTableColumn<AclRequestTableRow>> = [
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
      headerName: "Principals/Usernames",
      UNSAFE_render: ({ acl_ssl }: AclRequestTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {acl_ssl.map((ssl, index) => (
              <li key={`${ssl}-${index}`}>
                <StatusChip dense status="neutral" text={ssl} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "custom",
      headerName: "IP addresses",
      UNSAFE_render: ({ acl_ip }: AclRequestTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {acl_ip.map((ip, index) => (
              <li key={`${ip}-${index}`}>
                <StatusChip dense status="neutral" text={ip} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "status",
      headerName: "ACL type",
      status: ({ aclType }) => ({
        status: aclType === "CONSUMER" ? "success" : "info",
        text: aclType,
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
        onClick: () => onDetails(id),
        "aria-label": `View ACL request for ${topicname}`,
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
        disabled: !deletable,
        "aria-label": `Delete ACL request for ${topicname}`,
      }),
    },
  ];

  const rows: AclRequestTableRow[] = requests.map((request: AclRequest) => {
    return {
      id: Number(request.req_no),
      topicname: request.topicname,
      environmentName: request.environmentName,
      requestStatus: request.requestStatus,
      requestOperationType: request.requestOperationType,
      acl_ssl: request.acl_ssl,
      acl_ip: request.acl_ip,
      aclType: request.aclType,
      requesttimestring: request.requesttimestring,
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
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { AclRequestsTable, type AclRequestsTableProps };
