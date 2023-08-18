import {
  StatusChip,
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
} from "@aivenio/aquarium";
import { AclRequest } from "src/domain/acl/acl-types";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import infoSign from "@aivenio/aquarium/dist/src/icons/infoSign";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import {
  RequestOperationType,
  RequestStatus,
} from "src/domain/requests/requests-types";

interface AclRequestTableRow {
  id: number;
  acl_ssl: string[];
  acl_ip: string[];
  topicname: AclRequest["topicname"];
  prefixed: boolean;
  environmentName: string;
  teamname: AclRequest["teamname"];
  aclType: AclRequest["aclType"];
  requestor: string;
  requesttimestring: string;
  requestStatus: RequestStatus;
  requestOperationType: RequestOperationType;
}

export default function AclApprovalsTable({
  aclRequests,
  actionsDisabled = false,
  onDetails,
  onApprove,
  onDecline,
  isBeingApproved,
  isBeingDeclined,
  ariaLabel,
}: Props) {
  const getRows = (entries: AclRequest[]): AclRequestTableRow[] => {
    if (entries === undefined) {
      return [];
    }
    return entries.map(
      ({
        req_no,
        acl_ssl,
        acl_ip,
        topicname,
        aclPatternType,
        environmentName,
        teamname,
        aclType,
        requestor,
        requesttimestring,
        requestStatus,
        requestOperationType,
      }) => ({
        id: Number(req_no),
        acl_ssl,
        acl_ip,
        topicname: topicname,
        prefixed: aclPatternType === "PREFIXED",
        environmentName,
        teamname,
        aclType,
        requestor,
        requesttimestring,
        requestStatus,
        requestOperationType,
      })
    );
  };

  const columns: Array<DataTableColumn<AclRequestTableRow>> = [
    {
      type: "custom",
      headerName: "Topic",
      UNSAFE_render({ topicname, prefixed }: AclRequestTableRow) {
        return (
          <>
            {topicname}
            {prefixed && <code>(prefixed)</code>}
          </>
        );
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
      type: "text",
      field: "teamname",
      headerName: "Team",
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
      field: "requestor",
      headerName: "Requested by",
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
      headerName: "Details",
      headerInvisible: true,
      type: "action",
      action: (request: AclRequestTableRow) => ({
        onClick: () => onDetails(request.id),
        text: "View",
        "aria-label": `View ACL request for ${request.topicname}`,
        icon: infoSign,
      }),
    },
    {
      width: 30,
      headerName: "Approve",
      headerInvisible: true,
      type: "action",
      action: (request: AclRequestTableRow) => {
        const approveInProgress = isBeingApproved(request.id);
        const declineInProgress = isBeingDeclined(request.id);
        return {
          onClick: () => onApprove(request.id),
          text: "Approve",
          "aria-label": `Approve ACL request for ${request.topicname}`,
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
      width: 30,
      headerName: "Decline",
      headerInvisible: true,
      type: "action",
      action: (request: AclRequestTableRow) => {
        const approveInProgress = isBeingApproved(request.id);
        const declineInProgress = isBeingDeclined(request.id);
        return {
          onClick: () => onDecline(request.id),
          text: "Decline",
          "aria-label": `Decline ACL request for ${request.topicname}`,
          disabled:
            approveInProgress ||
            declineInProgress ||
            actionsDisabled ||
            request.requestStatus !== "CREATED",
          tooltip: `Decline ACL request for topic ${request.topicname}`,
          icon: declineInProgress ? loadingIcon : deleteIcon,
          loading: declineInProgress,
        };
      },
    },
  ];

  const rows = getRows(aclRequests);

  if (!rows.length) {
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

export type Props = {
  aclRequests: AclRequest[];
  actionsDisabled?: boolean;
  onDetails: (reqNo: number) => void;
  onApprove: (reqNo: number) => void;
  onDecline: (reqNo: number) => void;
  isBeingApproved: (reqNo: number) => boolean;
  isBeingDeclined: (reqNo: number) => boolean;
  ariaLabel: string;
};
