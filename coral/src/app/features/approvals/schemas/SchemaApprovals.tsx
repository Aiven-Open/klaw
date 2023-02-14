import { SearchInput, NativeSelect } from "@aivenio/aquarium";
import { Pagination } from "src/app/components/Pagination";
import SchemaApprovalsTable from "src/app/features/approvals/schemas/components/SchemaApprovalsTable";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";

const responseMock = [
  {
    req_no: 1014,
    topicname: "testtopic-first",
    environment: "1",
    environmentName: "BRG",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    req_no: 1013,
    topicname: "testtopic-second",
    environment: "2",
    environmentName: "SEC",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    topicstatus: "created",
    requesttype: "Create",
    forceRegister: false,
    remarks: "asap",
    approver: null,
    approvingtime: null,
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    totalNoPages: "1",
    allPageNos: ["1"],
    currentPage: "1",
  },
];

function changePage() {
  console.log("page changed");
}

function SchemaApprovals() {
  const filters = [
    <NativeSelect labelText={"Filter by topics"} key={"filter-topic"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,

    <NativeSelect labelText={"Filter by cluster"} key={"filter-cluster"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,

    <NativeSelect labelText={"Filter by status"} key={"filter-status"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,
    <div key={"search"}>
      <SearchInput
        type={"search"}
        aria-describedby={"search-field-description"}
        role="search"
        placeholder={"Search for..."}
      />
      <div id={"search-field-description"} className={"visually-hidden"}>
        Press &quot;Enter&quot; to start your search. Press &quot;Escape&quot;
        to delete all your input.
      </div>
    </div>,
  ];

  const table = <SchemaApprovalsTable requests={responseMock} />;
  const pagination = (
    <Pagination activePage={1} totalPages={2} setActivePage={changePage} />
  );

  return (
    <ApprovalsLayout filters={filters} table={table} pagination={pagination} />
  );
}

export default SchemaApprovals;
