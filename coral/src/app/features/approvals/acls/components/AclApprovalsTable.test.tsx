import { cleanup, screen, render, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AclRequest } from "src/domain/acl/acl-types";
import AclApprovalsTable from "src/app/features/approvals/acls/components/AclApprovalsTable";
import { type Props } from "src/app/features/approvals/acls/components/AclApprovalsTable";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const aclRequests: AclRequest[] = [
  {
    remarks: undefined,
    consumergroup: "-na-",
    acl_ip: undefined,
    acl_ssl: ["mbasani", "maulbach"],
    aclPatternType: "LITERAL",
    transactionalId: undefined,
    req_no: 1014,
    topicname: "aivtopic1",
    environment: "1",
    teamname: "Ospo",
    aclIpPrincipleType: "PRINCIPAL",
    environmentName: "DEV",
    teamId: 1003,
    requestingteam: 1003,
    requestingTeamName: "Ospo",
    appname: "App",
    username: "amathieu",
    requesttime: "2023-01-06T14:50:37.912+00:00",
    requesttimestring: "06-Jan-2023 14:50:37",
    requestStatus: "CREATED",
    approver: undefined,
    approvingtime: undefined,
    aclType: "CONSUMER",
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "2",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
    requestOperationType: "CREATE",
  },
  {
    remarks: "hello",
    consumergroup: undefined,
    acl_ip: ["3.3.3.32", "3.3.3.33"],
    acl_ssl: ["User:*"],
    aclPatternType: "PREFIXED",
    transactionalId: undefined,
    req_no: 1015,
    topicname: "newaudittopic",
    environment: "2",
    teamname: "Ospo",
    aclType: "PRODUCER",
    aclIpPrincipleType: "IP_ADDRESS",
    environmentName: "TST",
    teamId: 1003,
    requestingteam: 1003,
    appname: "App",
    username: "amathieu",
    requesttime: "2023-01-10T13:19:10.757+00:00",
    requesttimestring: "10-Jan-2023 13:19:10",
    requestStatus: "APPROVED",
    approver: undefined,
    approvingtime: undefined,
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "2",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
    requestOperationType: "CREATE",
  },
];

describe("AclApprovalsTable", () => {
  function renderFromProps(props?: Partial<Props>): void {
    render(
      <AclApprovalsTable
        aclRequests={aclRequests}
        activePage={1}
        totalPages={10}
        isBeingApproved={jest.fn()}
        onApprove={jest.fn()}
        onDecline={jest.fn()}
        onDetails={jest.fn()}
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

  it("describes the table content and pagination for screen readers", () => {
    renderFromProps();
    screen.getByLabelText(`Acl requests, page 1 of 10`);
  });

  it("has column to describe the usernames", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Principals/Usernames");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "mbasani"
    );
  });

  it("has column to describe the ip addresses", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[1]
    ).toHaveTextContent("IP addresses");
    expect(within(getNthRow(2)).getAllByRole("cell")[1]).toHaveTextContent(
      "3.3.3.32 3.3.3.33"
    );
  });

  it("has column to describe the topic", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("Topic");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "aivtopic1"
    );
  });

  it("has column to describe the environment", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("Environment");
    expect(within(getNthRow(1)).getAllByRole("cell")[3]).toHaveTextContent(
      "DEV"
    );
  });

  it("has column to describe the team", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("Team");
    expect(within(getNthRow(1)).getAllByRole("cell")[4]).toHaveTextContent(
      "Ospo"
    );
  });

  it("has column to decsribe the acl type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("ACL type");
    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "CONSUMER"
    );
  });

  it("has column to decsribe the status", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[6]
    ).toHaveTextContent("Status");
    expect(within(getNthRow(1)).getAllByRole("cell")[6]).toHaveTextContent(
      "Awaiting approval"
    );
  });

  it("has column to decsribe the request type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[7]
    ).toHaveTextContent("Request type");
    expect(within(getNthRow(1)).getAllByRole("cell")[7]).toHaveTextContent(
      "Create"
    );
  });

  it("has column to decsribe the author of the request", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[8]
    ).toHaveTextContent("Requested by");
    expect(within(getNthRow(1)).getAllByRole("cell")[8]).toHaveTextContent(
      "amathieu"
    );
  });

  it("has column to decsribe the timestamp when the request was made", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[9]
    ).toHaveTextContent("Requested on");
    expect(within(getNthRow(1)).getAllByRole("cell")[9]).toHaveTextContent(
      "06-Jan-2023 14:50:37 UTC"
    );
  });

  it("has column for action to view request details", async () => {
    const onDetails = jest.fn();
    renderFromProps({ onDetails });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[10]).getByRole(
        "button",
        {
          name: "View acl request for aivtopic1",
        }
      )
    );
    expect(onDetails).toHaveBeenNthCalledWith(1, String(aclRequests[0].req_no));
  });

  it("has column for action to approve request", async () => {
    const onApprove = jest.fn();
    renderFromProps({ onApprove });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[11]).getByRole(
        "button",
        {
          name: "Approve acl request for aivtopic1",
        }
      )
    );
    expect(onApprove).toHaveBeenNthCalledWith(1, String(aclRequests[0].req_no));
  });

  it("has column for action to decline request", async () => {
    const onDecline = jest.fn();
    renderFromProps({ onDecline });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[12]).getByRole(
        "button",
        {
          name: "Decline acl request for aivtopic1",
        }
      )
    );
    expect(onDecline).toHaveBeenNthCalledWith(1, String(aclRequests[0].req_no));
  });

  it("shows one header row and two data rows", () => {
    renderFromProps();
    const rows = screen.getAllByRole("row");
    expect(rows).toHaveLength(3);
    expect(rows[0]).toHaveTextContent("Principals/Usernames");
    expect(rows[1]).toHaveTextContent("mbasani");
    expect(rows[2]).toHaveTextContent("User:*");
  });

  it("renders (prefixed) in Topic cell when appropriate", () => {
    renderFromProps();
    const prefixedCells = screen.getAllByText("(prefixed)");
    const notPrefixedCells = screen.getAllByText("aivtopic1");

    expect(prefixedCells).toHaveLength(1);
    expect(notPrefixedCells).toHaveLength(1);
  });

  it("renders all values for cells who can have multiple values", () => {
    renderFromProps();
    const cells = screen.getAllByRole("cell");
    expect(
      cells.filter((cell) => {
        return cell.textContent === "mbasani maulbach ";
      })
    ).toHaveLength(1);
    expect(
      cells.filter((cell) => cell.textContent === "3.3.3.32 3.3.3.33 ")
    ).toHaveLength(1);
  });

  it("render action buttons when status is CREATED", () => {
    renderFromProps();
    const createdRow = screen.getAllByRole("row")[1];
    expect(createdRow).toContainElement(
      screen.getByRole("button", { name: /Approve/ })
    );
    expect(createdRow).toContainElement(
      screen.getByRole("button", { name: /Decline/ })
    );
  });

  it("does not render action buttons when status is not CREATED", () => {
    renderFromProps();
    const approvedRow = screen.getAllByRole("row")[2];
    expect(approvedRow).not.toContainElement(
      screen.getByRole("button", { name: /Approve/ })
    );
    expect(approvedRow).not.toContainElement(
      screen.getByRole("button", { name: /Decline/ })
    );
  });

  it("disables approve and decline buttons when actions are disabled", () => {
    renderFromProps({ actionsDisabled: true });
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[11]).getByRole(
        "button",
        {
          name: "Approve acl request for aivtopic1",
        }
      )
    ).toBeDisabled();
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[12]).getByRole(
        "button",
        {
          name: "Decline acl request for aivtopic1",
        }
      )
    ).toBeDisabled();
  });
});
