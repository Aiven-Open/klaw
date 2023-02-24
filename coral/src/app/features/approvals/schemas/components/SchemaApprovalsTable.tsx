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
import { Dispatch, SetStateAction } from "react";
import loadingIcon from "@aivenio/aquarium/icons/loading";

interface SchemaRequestTableData {
  id: SchemaRequest["req_no"];
  topicname: SchemaRequest["topicname"];
  environmentName: SchemaRequest["environmentName"];
  username: SchemaRequest["username"];
  requesttimestring: SchemaRequest["requesttimestring"];
  requestStatus: SchemaRequest["requestStatus"];
}

type SchemaApprovalsTableProps = {
  requests: SchemaRequest[];
  setDetailsModal: Dispatch<
    SetStateAction<{
      isOpen: boolean;
      req_no: number | null;
    }>
  >;
};
function SchemaApprovalsTable(props: SchemaApprovalsTableProps) {
  const { requests, setDetailsModal } = props;
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
    { type: "text", field: "username", headerName: "Requested by" },
    {
      type: "text",
      field: "requesttimestring",
      headerName: "Date requested",
      formatter: (value) => {
        return `${value} UTC`;
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
            onClick={() =>
              setDetailsModal({ isOpen: true, req_no: request.id })
            }
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
        return (
          <GhostButton
            onClick={() => alert("Approve")}
            title={"Approve request"}
            aria-label={`Approve schema request for ${request.topicname}`}
          >
            <Icon color="grey-70" icon={tickCircle} />
          </GhostButton>
        );
      },
    },
    {
      type: "custom",
      headerName: "Decline",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: (request) => {
        return (
          <GhostButton
            onClick={() => alert("Decline")}
            aria-label={`Decline schema request for ${request.topicname}`}
            title={"Decline request"}
          >
            <Icon color="grey-70" icon={deleteIcon} />
          </GhostButton>
        );
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
