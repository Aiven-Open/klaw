import { NativeSelect, SearchInput } from "@aivenio/aquarium";
import { ApprovalsLayout } from "src/app/features/approvals/components/ApprovalsLayout";
import { Pagination } from "src/app/components/Pagination";
import { TopicApprovalsTable } from "src/app/features/approvals/topics/components/TopicApprovalsTable";
import { TopicRequestTypes, TopicRequestStatus } from "src/domain/topic";

const mockRequests = [
  {
    topicname: "test-topic-1",
    environment: "1",
    topicpartitions: 4,
    teamname: "Ospo",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "DEV",
    topicid: 1034,
    advancedTopicConfigEntries: [
      {
        configKey: "compression.type",
        configValue: "snappy",
      },
      {
        configKey: "cleanup.policy",
        configValue: "delete",
      },
    ],
    topictype: "Create" as TopicRequestTypes,
    requestor: "samulisuortti",
    requesttime: "2023-02-03T13:27:17.252+00:00",
    requesttimestring: "03-Feb-2023 13:27:17",
    topicstatus: "created" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,mirjamaulbach,smustafa,amathieu,aindriul,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    topicname: "test-topic-2",
    environment: "1",
    topicpartitions: 4,
    teamname: "Ospo",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "DEV",
    topicid: 1034,
    advancedTopicConfigEntries: [
      {
        configKey: "compression.type",
        configValue: "snappy",
      },
      {
        configKey: "cleanup.policy",
        configValue: "delete",
      },
    ],

    topictype: "Create" as TopicRequestTypes,
    requestor: "samulisuortti",
    requesttime: "2023-02-03T13:27:17.252+00:00",
    requesttimestring: "03-Feb-2023 13:27:17",
    topicstatus: "created" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,mirjamaulbach,smustafa,amathieu,aindriul,",
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

  const table = <TopicApprovalsTable requests={mockRequests} />;
  const pagination = (
    <Pagination activePage={1} totalPages={2} setActivePage={changePage} />
  );

  return (
    <ApprovalsLayout filters={filters} table={table} pagination={pagination} />
  );
}

export default TopicApprovals;
