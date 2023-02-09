import {
  Icon,
  DataTable,
  DataTableColumn,
  GhostButton,
} from "@aivenio/aquarium";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";
import tickCircle from "@aivenio/aquarium/dist/src/icons/tickCircle";

const responseMock = [
  {
    req_no: 1014,
    topicname: "uptimetopic",
    environment: "3",
    environmentName: "DEV",
    schemaversion: "1.0",
    teamname: "Ospo",
    teamId: 1003,
    appname: "App",
    schemafull:
      '{\n   "type" : "record",\n   "namespace" : "Tutorialspoint",\n   "name" : "Employee",\n   "fields" : [\n      { "name" : "Name" , "type" : "string" },\n      { "name" : "Age" , "type" : "int" }\n   ]\n}',
    username: "samulisuortti",
    requesttime: "2023-02-03T13:28:28.061+00:00",
    requesttimestring: "03-Feb-2023 13:28:28",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,mirjamaulbach,smustafa,amathieu,aindriul,",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    req_no: 1013,
    topicname: "testopic",
    environment: "3",
    environmentName: "INFRA",
    schemaversion: "1.0",
    teamname: "Ospo",
    teamId: 1003,
    appname: "App",
    schemafull:
      '{\n   "type" : "record",\n   "namespace" : "Tutorialspoint",\n   "name" : "Employee",\n   "fields" : [\n      { "name" : "Name" , "type" : "string" },\n      { "name" : "Age" , "type" : "int" }\n   ]\n}',
    username: "samulisuortti",
    requesttime: "2023-02-03T13:28:28.061+00:00",
    requesttimestring: "03-Feb-2023 13:28:28",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,mirjamaulbach,smustafa,amathieu,aindriul,",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
];

interface SchemaRequestTableData {
  id: number;
  topicname: string;
  environmentName: string;
  username: string;
  requesttimestring: string;
}

const columns: Array<DataTableColumn<SchemaRequestTableData>> = [
  { type: "text", field: "topicname", headerName: "Topic" },
  { type: "text", field: "environmentName", headerName: "Cluster" },
  { type: "text", field: "username", headerName: "Requested by" },
  {
    type: "text",
    field: "requesttimestring",
    headerName: "Date Requested",
  },
  {
    type: "custom",
    headerName: "",
    width: 30,
    UNSAFE_render: (row) => {
      return (
        <GhostButton
          onClick={() => alert("Approve")}
          title={`Approve schema request for ${row.topicname}`}
        >
          <Icon color="grey-70" icon={tickCircle} />
        </GhostButton>
      );
    },
  },
  {
    type: "custom",
    headerName: "",
    width: 30,
    UNSAFE_render: (row) => {
      return (
        <GhostButton
          onClick={() => alert("Decline")}
          title={`Decline schema request for ${row.topicname}`}
        >
          <Icon color="grey-70" icon={deleteIcon} />
        </GhostButton>
      );
    },
  },
];

const rows: SchemaRequestTableData[] = responseMock.map((request) => {
  return {
    id: request.req_no,
    topicname: request.topicname,
    environmentName: request.environmentName,
    username: request.username,
    requesttimestring: request.requesttimestring,
  };
});

function SchemaApprovals() {
  return (
    <div className={"a11y-enhancement-data-table"}>
      <DataTable
        ariaLabel={"Schema requests"}
        columns={columns}
        rows={rows}
        noWrap={false}
      />
    </div>
  );
}

export default SchemaApprovals;
