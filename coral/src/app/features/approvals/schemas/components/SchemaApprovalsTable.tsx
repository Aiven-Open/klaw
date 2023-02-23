import { DataTable, DataTableColumn, GhostButton } from "@aivenio/aquarium";
import { SchemaRequest } from "src/domain/schema-request";
import infoSign from "@aivenio/aquarium/icons/infoSign";
import tickCircle from "@aivenio/aquarium/icons/tickCircle";
import deleteIcon from "@aivenio/aquarium/icons/delete";
import {
  getRequestStatusColor,
  getRequestStatusName,
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
    { type: "text", field: "environmentName", headerName: "Environment" },
    {
    type: "status",
    field: "requestStatus",
    headerName: "Status",
    status: ({ requestStatus }) => {
      return {
        status: getRequestStatusColor(requestStatus),
        text: getRequestStatusName(requestStatus),
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
      // @TODO PR open in DS to be able to
      // add an "invisible" header name that
      // is used as aria-label. That will also
      // solve the duplicate key warning
      //https://github.com/aiven/design-system/pull/950
      headerName: "Details",
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
      // @TODO PR open in DS to be able to
      // add an "invisible" header name that
      // is used as aria-label. That will also
      // solve the duplicate key warning
      //https://github.com/aiven/design-system/pull/950
      headerName: "Approve",
      width: 30,
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Approve")}
            aria-label={`Approve schema request for ${row.topicname}`}
            icon={tickCircle}
          />
        );
      },
    },
    {
      type: "custom",
      // @TODO PR open in DS to be able to
      // add an "invisible" header name that
      // is used as aria-label. That will also
      // solve the duplicate key warning
      //https://github.com/aiven/design-system/pull/950
      headerName: "Decline",
      width: 30,
      UNSAFE_render: (row) => {
        return (
          <GhostButton
            onClick={() => alert("Decline")}
            aria-label={`Decline schema request for ${row.topicname}`}
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
