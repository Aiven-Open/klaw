import { cleanup, render, screen, within } from "@testing-library/react";
import { TopicRequest } from "src/domain/topic";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import {
  TopicRequestsTable,
  type TopicRequestsTableProps,
} from "src/app/features/requests/topics/components/TopicRequestsTable";
import userEvent from "@testing-library/user-event";

const mockedRequests: TopicRequest[] = [
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
    requestOperationType: "CREATE",
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    requestStatus: "CREATED",
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
    editable: true,
    deletable: true,
    deleteAssociatedSchema: false,
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

    requestOperationType: "UPDATE",
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    requestStatus: "APPROVED",
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
    editable: true,
    deletable: true,
    deleteAssociatedSchema: false,
  },
];

describe("TopicRequestsTable", () => {
  function renderFromProps(props?: Partial<TopicRequestsTableProps>): void {
    render(
      <TopicRequestsTable
        requests={mockedRequests}
        onDetails={jest.fn()}
        onDelete={jest.fn()}
        {...props}
      />
    );
  }

  function getNthRow(nth: number): HTMLElement {
    return screen.getAllByRole("row")[nth];
  }

  beforeEach(() => {
    mockIntersectionObserver();
  });

  afterEach(cleanup);

  it("shows a message to user in case there are no requests that match the search criteria", () => {
    renderFromProps({ requests: [] });
    screen.getByText("No Topic requests");
    screen.getByText("No Topic request matched your criteria.");
  });

  it("has column to describe the topic", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Topic");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "test-topic-1"
    );
  });

  it("has column to describe the environment", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[1]
    ).toHaveTextContent("Environment");
    expect(within(getNthRow(1)).getAllByRole("cell")[1]).toHaveTextContent(
      "BRG"
    );
  });

  it("has column to decsribe the status", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("Status");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "Awaiting approval"
    );
  });

  it("has column to decsribe the request type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("Type");
    expect(within(getNthRow(1)).getAllByRole("cell")[3]).toHaveTextContent(
      "Create"
    );
  });

  it("has column to describe the owner", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("Owned by");
    expect(within(getNthRow(1)).getAllByRole("cell")[4]).toHaveTextContent(
      "NCC1701D"
    );
  });

  it("has column to decsribe the author of the request", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("Requested by");
    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "jlpicard"
    );
  });

  it("has column to decsribe the timestamp when the request was made", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[6]
    ).toHaveTextContent("Requested on");
    expect(within(getNthRow(1)).getAllByRole("cell")[6]).toHaveTextContent(
      "28-Sep-1987 13:37:00 UTC"
    );
  });

  it("has column for action to view request details", async () => {
    const onDetails = jest.fn();
    renderFromProps({ onDetails });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[7]).getByRole("button", {
        name: "View",
      })
    );
    expect(onDetails).toHaveBeenNthCalledWith(1, mockedRequests[0].topicid);
  });

  it("has column for action to delete request", async () => {
    const onDelete = jest.fn();
    renderFromProps({ onDelete });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[8]).getByRole("button", {
        name: "Delete topic request for test-topic-1",
      })
    );
    expect(onDelete).toHaveBeenNthCalledWith(1, mockedRequests[0].topicid);
  });

  it("disables the delete button for a request if the request is not deletable", () => {
    const onDelete = jest.fn();
    const nonDeletableRequest = { ...mockedRequests[0], deletable: false };
    renderFromProps({ requests: [nonDeletableRequest], onDelete });
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[8]).getByRole("button", {
        name: "Delete topic request for test-topic-1",
      })
    ).toBeDisabled();
  });
});
