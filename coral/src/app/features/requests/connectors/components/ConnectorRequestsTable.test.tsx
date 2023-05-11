import { cleanup, render, screen, within } from "@testing-library/react";
import { ConnectorRequest } from "src/domain/connector";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import {
  ConnectorRequestsTable,
  type ConnectorRequestsTableProps,
} from "src/app/features/requests/connectors/components/ConnectorRequestsTable";
import userEvent from "@testing-library/user-event";

const mockedRequests: ConnectorRequest[] = [
  {
    connectorName: "test-connector-1",
    environment: "1",
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This connector is for test",
    environmentName: "BRG",
    connectorId: 1000,
    connectorConfig: "",
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
  },
  {
    connectorName: "test-connector-2",
    environment: "1",
    teamname: "MIRRORUNIVERSE",
    remarks: "asap",
    description: "This connector is for test",
    environmentName: "SBY",
    connectorId: 1001,
    connectorConfig: "",
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
  },
];

describe("ConnectorRequestsTable", () => {
  function renderFromProps(props?: Partial<ConnectorRequestsTableProps>): void {
    render(
      <ConnectorRequestsTable
        requests={mockedRequests}
        onDetails={vi.fn()}
        onDelete={vi.fn()}
        ariaLabel={"Connector requests"}
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
    screen.getByText("No Connector requests");
    screen.getByText("No Connector request matched your criteria.");
  });

  it("has column to describe the connector name", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Name");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "test-connector-1"
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

  it("has column to describe the owner", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("Owned by");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "NCC1701D"
    );
  });

  it("has column to describe the status", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("Status");
    expect(within(getNthRow(1)).getAllByRole("cell")[3]).toHaveTextContent(
      "Awaiting approval"
    );
  });

  it("has column to describe the request type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("Request type");
    expect(within(getNthRow(1)).getAllByRole("cell")[4]).toHaveTextContent(
      "Create"
    );
  });

  it("has column to describe the author of the request", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("Requested by");
    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "jlpicard"
    );
  });

  it("has column to describe the timestamp when the request was made", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[6]
    ).toHaveTextContent("Requested on");
    expect(within(getNthRow(1)).getAllByRole("cell")[6]).toHaveTextContent(
      "28-Sep-1987 13:37:00 UTC"
    );
  });

  it("has column for action to view request details", async () => {
    const onDetails = vi.fn();
    renderFromProps({ onDetails });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[7]).getByRole("button", {
        name: "View connector request for test-connector-1",
      })
    );
    expect(onDetails).toHaveBeenNthCalledWith(1, mockedRequests[0].connectorId);
  });

  it("has column for action to delete request", async () => {
    const onDelete = vi.fn();
    renderFromProps({ onDelete });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[8]).getByRole("button", {
        name: "Delete connector request for test-connector-1",
      })
    );
    expect(onDelete).toHaveBeenNthCalledWith(1, mockedRequests[0].connectorId);
  });

  it("disables the delete button for a request if the request is not deletable", () => {
    const onDelete = vi.fn();
    const nonDeletableRequest = { ...mockedRequests[0], deletable: false };
    renderFromProps({ requests: [nonDeletableRequest], onDelete });
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[8]).getByRole("button", {
        name: "Delete connector request for test-connector-1",
      })
    ).toBeDisabled();
  });
});
