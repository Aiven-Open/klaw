import { DataTable, DataTableColumn, GhostButton } from "@aivenio/aquarium";
import { SchemaRequest } from "src/domain/schema-request";
import infoSign from "@aivenio/aquarium/icons/infoSign";
import tickCircle from "@aivenio/aquarium/icons/tickCircle";
import deleteIcon from "@aivenio/aquarium/icons/delete";
import {
  requestStatusChipStatusMap,
  requestStatusNameMap,
} from "src/app/features/approvals/utils/request-status-helper";
import { Dispatch, SetStateAction } from "react";

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
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => setDetailsModal({ isOpen: true, req_no: row.id })}
            icon={infoSign}
            dense
          >
            <span aria-hidden={"true"}>View details</span>
            <span className={"visually-hidden"}>
              View schema request for {row.topicname}
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
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Approve")}
            aria-label={`Approve schema request for ${row.topicname}`}
            title={`Approve schema request`}
            icon={tickCircle}
          />
        );
      },
    },
    {
      type: "custom",
      headerName: "Decline",
      headerInvisible: true,
      width: 30,
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Decline")}
            aria-label={`Decline schema request for ${row.topicname}`}
            title={`Decline schema request`}
            icon={deleteIcon}
          />
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
