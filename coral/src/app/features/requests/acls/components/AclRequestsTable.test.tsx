import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  AclRequestsTable,
  AclRequestsTableProps,
} from "src/app/features/requests/acls/components/AclRequestsTable";
import { AclRequest } from "src/domain/acl/acl-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedRequests: AclRequest[] = [
  {
    remarks: undefined,
    consumergroup: "-na-",
    acl_ip: [],
    acl_ssl: ["josepprat"],
    aclPatternType: "LITERAL",
    transactionalId: undefined,
    req_no: 1220,
    topicname: "uptimetopic",
    environment: "1",
    teamname: "Ospo",
    aclType: "PRODUCER",
    aclIpPrincipleType: "PRINCIPAL",
    requestStatus: "CREATED",
    requestOperationType: "CREATE",
    environmentName: "DEV",
    teamId: 1003,
    requestingteam: 1003,
    requestingTeamName: "Ospo",
    appname: "App",
    requestor: "josepprat",
    requesttime: "2023-03-10T12:08:46.040+00:00",
    requesttimestring: "10-Mar-2023 12:08:46",
    approver: undefined,
    approvingtime: undefined,
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "22",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,calummuir,roopek,MikeTest,Mischa,",
    deletable: false,
    editable: false,
  },

  {
    remarks: undefined,
    consumergroup: "sdsdsds",
    acl_ip: ["1.1.1.1", "2.2.2.2"],
    acl_ssl: ["User:*"],
    aclPatternType: "LITERAL",
    transactionalId: undefined,
    req_no: 1217,
    topicname: "newaudittopic",
    environment: "2",
    teamname: "Ospo",
    aclType: "CONSUMER",
    aclIpPrincipleType: "IP_ADDRESS",
    requestStatus: "DECLINED",
    requestOperationType: "CREATE",
    environmentName: "TST",
    teamId: 1003,
    requestingteam: 1003,
    requestingTeamName: "Ospo",
    appname: "App",
    requestor: "josepprat",
    requesttime: "2023-03-03T07:18:23.687+00:00",
    requesttimestring: "03-Mar-2023 07:18:23",
    approver: "amathieu",
    approvingtime: "2023-03-10T09:12:21.328+00:00",
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "22",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,calummuir,roopek,MikeTest,Mischa,",
    deletable: true,
    editable: true,
  },
];

describe("AclRequestsTable", () => {
  function renderFromProps(props?: Partial<AclRequestsTableProps>): void {
    render(
      <AclRequestsTable
        requests={mockedRequests}
        onDetails={vi.fn()}
        onDelete={vi.fn()}
        ariaLabel={"ACL requests, page 1 of 22"}
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
    screen.getByText("No ACL requests");
    screen.getByText("No ACL request matched your criteria.");
  });

  it("has column to describe the topic", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Topic");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "uptimetopic"
    );
  });

  it("has column to describe the environment", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[1]
    ).toHaveTextContent("Environment");
    expect(within(getNthRow(1)).getAllByRole("cell")[1]).toHaveTextContent(
      "DEV"
    );
  });

  it("has column to describe the Principals/Usernames", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("Principals/Usernames");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "josepprat"
    );
  });

  it("has column to describe the IP addresses", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("IP addresses");
    expect(within(getNthRow(2)).getAllByRole("cell")[3]).toHaveTextContent(
      "1.1.1.1 2.2.2.2"
    );
  });

  it("has column to describe the ACL type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("ACL type");
    expect(within(getNthRow(1)).getAllByRole("cell")[4]).toHaveTextContent(
      "PRODUCER"
    );
  });

  it("has column to describe the status", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("Status");
    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "Awaiting approval"
    );
  });

  it("has column to describe the request type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[6]
    ).toHaveTextContent("Request type");
    expect(within(getNthRow(1)).getAllByRole("cell")[6]).toHaveTextContent(
      "Create"
    );
  });

  it("has column for action to view request details", async () => {
    const onDetails = vi.fn();
    renderFromProps({ onDetails });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[8]).getByRole("button", {
        name: `View ACL request for ${mockedRequests[0].topicname}`,
      })
    );
    expect(onDetails).toHaveBeenNthCalledWith(1, mockedRequests[0].req_no);
  });

  it("has column for action to delete request", async () => {
    const onDelete = vi.fn();
    renderFromProps({ onDelete });
    await userEvent.click(
      within(within(getNthRow(2)).getAllByRole("cell")[9]).getByRole("button", {
        name: `Delete ACL request for ${mockedRequests[1].topicname}`,
      })
    );
    expect(onDelete).toHaveBeenNthCalledWith(1, mockedRequests[1].req_no);
  });

  it("disables the delete button for a request if the request is not deletable", () => {
    const onDelete = vi.fn();
    const nonDeletableRequest = { ...mockedRequests[0], deletable: false };
    renderFromProps({ requests: [nonDeletableRequest], onDelete });
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[9]).getByRole("button", {
        name: `Delete ACL request for ${mockedRequests[0].topicname}`,
      })
    ).toBeDisabled();
  });
});
