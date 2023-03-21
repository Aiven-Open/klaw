import {
  GhostButton,
  Icon,
  StatusChip,
  Flexbox,
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
  // `requestStatus` is always defined from backend
  // but api definition says it can be undefined
  // the empty string is used to make ts compiler
  // happy :D
  requestStatus: RequestStatus | "";
  requestOperationType: RequestOperationType | "";
}

export default function AclApprovalsTable({
  aclRequests,
  activePage,
  totalPages,
  actionsDisabled,
  isBeingApproved,
  onDetails,
  onApprove,
  onDecline,
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
        acl_ssl: acl_ssl ?? [],
        acl_ip: acl_ip ?? [],
        topicname: topicname,
        prefixed: aclPatternType === "PREFIXED",
        environmentName: environmentName ?? "-",
        teamname,
        aclType,
        requestor: requestor ?? "-",
        requesttimestring: requesttimestring ?? "-",
        requestStatus: requestStatus ?? "",
        requestOperationType: requestOperationType ?? "",
      })
    );
  };

  const columns: Array<DataTableColumn<AclRequestTableRow>> = [
    {
      type: "custom",
      field: "topicname",
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
        if (requestStatus === "") {
          return {
            status: "neutral",
            text: "-",
          };
        }
        return {
          status: requestStatusChipStatusMap[requestStatus],
          text: requestStatusNameMap[requestStatus],
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
      type: "text",
      field: "teamname",
      headerName: "Team",
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
      type: "status",
      field: "requestOperationType",
      headerName: "Request type",
      status: ({ requestOperationType }) => {
        if (requestOperationType === "") {
          return {
            status: "neutral",
            text: "-",
          };
        }
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
      type: "custom",
      UNSAFE_render: ({ id, topicname }: AclRequestTableRow) => {
        return (
          <GhostButton
            icon={infoSign}
            aria-label={`View acl request for ${topicname}`}
            onClick={() => onDetails(String(id))}
            dense
          >
            <span aria-hidden={"true"}>View details</span>
            <span className={"visually-hidden"}>
              View topic request for {topicname}
            </span>
          </GhostButton>
        );
      },
    },
    {
      width: 30,
      headerName: "Approve",
      headerInvisible: true,
      type: "custom",
      UNSAFE_render: ({ id, requestStatus, topicname }: AclRequestTableRow) => {
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => {
                onApprove(String(id));
              }}
              title={"Approve acl request"}
              aria-label={`Approve acl request for ${topicname}`}
              disabled={actionsDisabled}
            >
              {isBeingApproved(String(id)) ? (
                <Icon color="grey-70" icon={loadingIcon} />
              ) : (
                <Icon color="grey-70" icon={tickCircle} />
              )}
            </GhostButton>
          );
        }
      },
    },
    {
      width: 30,
      headerName: "Decline",
      headerInvisible: true,
      type: "custom",
      UNSAFE_render: ({ id, requestStatus, topicname }: AclRequestTableRow) => {
        if (requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => onDecline(String(id))}
              title={`Decline acl request`}
              aria-label={`Decline acl request for ${topicname}`}
              disabled={actionsDisabled}
            >
              <Icon color="grey-70" icon={deleteIcon} />
            </GhostButton>
          );
        }
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
      ariaLabel={`Acl requests, page ${activePage} of ${totalPages}`}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export type Props = {
  aclRequests: AclRequest[];
  activePage: number;
  totalPages: number;
  actionsDisabled?: boolean;
  isBeingApproved: (reqNo: string) => boolean;
  onDetails: (reqNo: string) => void;
  onApprove: (reqNo: string) => void;
  onDecline: (reqNo: string) => void;
};
