import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { Pagination } from "src/app/components/Pagination";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { TopicRequestTypes, TopicRequestStatus } from "src/domain/topic";

const mockedRequests = [
  {
    topicname: "test-topic-1",
    environment: "1",
    topicpartitions: 4,
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "BRG",
    topicid: 1000,
    advancedTopicConfigEntries: [
      {
        configKey: "cleanup.policy",
        configValue: "delete",
      },
    ],
    topictype: "Create" as TopicRequestTypes,
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    topicstatus: "created" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    topicname: "test-topic-2",
    environment: "1",
    topicpartitions: 4,
    teamname: "MIRRORUNIVERSE",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "SBY",
    topicid: 1001,
    advancedTopicConfigEntries: [
      {
        configKey: "compression.type",
        configValue: "snappy",
      },
    ],

    topictype: "Update" as TopicRequestTypes,
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    topicstatus: "approved" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
];

function TopicApprovals() {
  function changePage() {
    console.log("changed page!");
  }

  const filters = [
    <NativeSelect labelText={"Filter by teams"} key={"filter-teams"}>
      <option> one </option>
      <option> two </option>
      <option> three </option>
    </NativeSelect>,

    <NativeSelect
      labelText={"Filter by environment"}
      key={"filter-environments"}
    >
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

  const table = <TopicApprovalsTable requests={mockedRequests} />;
  const pagination = (
    <Pagination activePage={1} totalPages={2} setActivePage={changePage} />
  );

  return (
    <ApprovalsLayout filters={filters} table={table} pagination={pagination} />
  );
}

export default TopicApprovals;
