import {
  DataTable,
  DataTableColumn,
  GhostButton,
  Icon,
} from "@aivenio/aquarium";
import { SchemaRequest } from "src/domain/schema-request";
import infoSign from "@aivenio/aquarium/icons/infoSign";
import tickCircle from "@aivenio/aquarium/icons/tickCircle";
import deleteIcon from "@aivenio/aquarium/icons/delete";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import { Dispatch, SetStateAction, useState } from "react";
import loadingIcon from "@aivenio/aquarium/icons/loading";
import {
  requestOperationTypeChipStatusMap,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";

interface SchemaRequestTableData {
  id: SchemaRequest["req_no"];
  topicname: SchemaRequest["topicname"];
  environmentName: SchemaRequest["environmentName"];
  username: SchemaRequest["username"];
  requesttimestring: SchemaRequest["requesttimestring"];
  requestStatus: SchemaRequest["requestStatus"];
  requestOperationType: SchemaRequest["requestOperationType"];
}

type SchemaApprovalsTableProps = {
  requests: SchemaRequest[];
  setModals: Dispatch<
    SetStateAction<{
      open: "DETAILS" | "DECLINE" | "NONE";
      req_no: number | null;
    }>
  >;
  quickActionLoading: boolean;
  onApprove: (req_no: number) => void;
};

function SchemaApprovalsTable(props: SchemaApprovalsTableProps) {
  const { requests, setModals, quickActionLoading, onApprove } = props;
  const columns: Array<DataTableColumn<SchemaRequestTableData>> = [
    { type: "text", field: "topicname", headerName: "Topic" },
    {
      type: "status",
      field: "environmentName",
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
      headerName: "Request type",
      status: ({ requestOperationType }) => {
        return {
          status: requestOperationTypeChipStatusMap[requestOperationType],
          text: requestOperationTypeNameMap[requestOperationType],
        };
      },
    },
    { type: "text", field: "username", headerName: "Requested by" },
    {
      type: "text",
      field: "requesttimestring",
      headerName: "Requested on",
      formatter: (value) => {
        return `${value}${"\u00A0"}UTC`;
      },
    },
    {
      type: "custom",
      headerName: "Details",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: (request) => {
        return (
          <GhostButton
            onClick={() => setModals({ open: "DETAILS", req_no: request.id })}
            icon={infoSign}
            dense
          >
            <span aria-hidden={"true"}>View details</span>
            <span className={"visually-hidden"}>
              View schema request for {request.topicname}
            </span>
          </GhostButton>
        );
      },
    },
    {
      type: "custom",
      headerName: "Approve",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: (request) => {
        if (request.requestStatus === "CREATED") {
          const [isLoading, setIsLoading] = useState(false);
          return (
            <GhostButton
              onClick={() => {
                setIsLoading(true);
                onApprove(request.id);
              }}
              title={"Approve request"}
              aria-label={`Approve schema request for ${request.topicname}`}
              disabled={quickActionLoading}
            >
              {isLoading && quickActionLoading ? (
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
      type: "custom",
      headerName: "Decline",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: (request) => {
        if (request.requestStatus === "CREATED") {
          return (
            <GhostButton
              onClick={() => setModals({ open: "DECLINE", req_no: request.id })}
              aria-label={`Decline schema request for ${request.topicname}`}
              title={"Decline request"}
              disabled={quickActionLoading}
            >
              <Icon color="grey-70" icon={deleteIcon} />
            </GhostButton>
          );
        }
      },
    },
  ];
  const rows: SchemaRequestTableData[] = requests.map(
    (request: SchemaRequest) => {
      return {
        id: request.req_no,
        topicname: request.topicname,
        environmentName: request.environmentName,
        username: request.username,
        requesttimestring: request.requesttimestring,
        requestStatus: request.requestStatus,
        requestOperationType: request.requestOperationType,
      };
    }
  );

  return (
    <DataTable
      ariaLabel={"Schema requests"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export default SchemaApprovalsTable;
