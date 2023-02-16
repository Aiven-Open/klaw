import { DataTable, DataTableColumn, GhostButton } from "@aivenio/aquarium";
import { CreatedSchemaRequests } from "src/domain/schema-request/schema-request-types";
import infoSign from "@aivenio/aquarium/icons/infoSign";
import tickCircle from "@aivenio/aquarium/icons/tickCircle";
import deleteIcon from "@aivenio/aquarium/icons/delete";

interface SchemaRequestTableData {
  id: number;
  topicname: string;
  environmentName: string;
  username: string;
  requesttimestring: string;
}

const columns: Array<DataTableColumn<SchemaRequestTableData>> = [
  { type: "text", field: "topicname", headerName: "Topic" },
  { type: "text", field: "environmentName", headerName: "Environment" },
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
        <GhostButton onClick={() => alert("Approve")} icon={infoSign} dense>
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

type SchemaApprovalsTableProps = {
  requests: CreatedSchemaRequests[];
};
function SchemaApprovalsTable(props: SchemaApprovalsTableProps) {
  const { requests } = props;

  const rows: SchemaRequestTableData[] = requests.map(
    (request: CreatedSchemaRequests) => {
      return {
        id: request.req_no,
        topicname: request.topicname,
        environmentName: request.environmentName,
        username: request.username,
        requesttimestring: request.requesttimestring,
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
