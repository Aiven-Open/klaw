import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AclApprovalsTable, {
  type Props,
} from "src/app/features/approvals/acls/components/AclApprovalsTable";
import { AclRequest } from "src/domain/acl/acl-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const aclRequests: AclRequest[] = [
  {
    remarks: undefined,
    consumergroup: "-na-",
    acl_ip: [],
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
    requestor: "amathieu",
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
    consumergroup: "-na-",
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
    requestingTeamName: "Ospo",
    requestor: "amathieu",
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
        ariaLabel={"ACL approval requests, page 1 of 10"}
        isBeingApproved={jest.fn()}
        isBeingDeclined={jest.fn()}
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

  it("shows a message to user in case there are no requests that match the search criteria", () => {
    renderFromProps({ aclRequests: [] });
    screen.getByText("No ACL requests");
    screen.getByText("No ACL request matched your criteria.");
  });

  it("describes the table content and pagination for screen readers", () => {
    renderFromProps();
    screen.getByLabelText(`ACL approval requests, page 1 of 10`);
  });

  it("has column to describe the topic", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Topic");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "aivtopic1"
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

  it("has column to decsribe the status", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("Status");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "Awaiting approval"
    );
  });

  it("has column to describe the usernames", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("Principals/Usernames");
    expect(within(getNthRow(1)).getAllByRole("cell")[3]).toHaveTextContent(
      "mbasani"
    );
  });

  it("has column to describe the list with ip addresses", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("IP addresses");

    const row = within(getNthRow(2)).getAllByRole("cell")[4];

    expect(within(row).getAllByRole("listitem")[0]).toHaveTextContent(
      "3.3.3.32"
    );
    expect(within(row).getAllByRole("listitem")[1]).toHaveTextContent(
      "3.3.3.33"
    );
  });

  it("has column to describe the team", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("Team");

    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "Ospo"
    );
  });

  it("has column to decsribe the acl type", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[6]
    ).toHaveTextContent("ACL type");
    expect(within(getNthRow(1)).getAllByRole("cell")[6]).toHaveTextContent(
      "CONSUMER"
    );
  });

  it("has column to describe the request type", () => {
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
          name: "View ACL request for aivtopic1",
        }
      )
    );
    expect(onDetails).toHaveBeenNthCalledWith(1, aclRequests[0].req_no);
  });

  it("has column for action to approve request", async () => {
    const onApprove = jest.fn();
    renderFromProps({ onApprove });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[11]).getByRole(
        "button",
        {
          name: "Approve ACL request for aivtopic1",
        }
      )
    );
    expect(onApprove).toHaveBeenNthCalledWith(1, aclRequests[0].req_no);
  });

  it("has column for action to decline request", async () => {
    const onDecline = jest.fn();
    renderFromProps({ onDecline });
    await userEvent.click(
      within(within(getNthRow(1)).getAllByRole("cell")[12]).getByRole(
        "button",
        {
          name: "Decline ACL request for aivtopic1",
        }
      )
    );
    expect(onDecline).toHaveBeenNthCalledWith(1, aclRequests[0].req_no);
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

  it("renders a list for cells who can have multiple values", () => {
    renderFromProps();
    const cells = screen.getAllByRole("cell");

    const userNames = cells.filter((cell) => {
      return cell.textContent === "mbasanimaulbach";
    })[0];

    expect(within(userNames).getByRole("list")).toBeVisible();
    expect(within(userNames).getAllByRole("listitem")).toHaveLength(2);

    const ipAdresses = cells.filter((cell) => {
      return cell.textContent === "3.3.3.323.3.3.33";
    })[0];

    expect(within(ipAdresses).getByRole("list")).toBeVisible();
    expect(within(ipAdresses).getAllByRole("listitem")).toHaveLength(2);
  });

  it("disables action buttons when status is not CREATED", () => {
    renderFromProps();
    const approvedRow = screen.getAllByRole("row")[2];
    expect(
      within(approvedRow).getByRole("button", { name: /Approve/ })
    ).toBeDisabled();
    expect(
      within(approvedRow).getByRole("button", { name: /Decline/ })
    ).toBeDisabled();
  });

  it("disables approve and decline buttons when actions are disabled", () => {
    renderFromProps({ actionsDisabled: true });
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[11]).getByRole(
        "button",
        {
          name: "Approve ACL request for aivtopic1",
        }
      )
    ).toBeDisabled();
    expect(
      within(within(getNthRow(1)).getAllByRole("cell")[12]).getByRole(
        "button",
        {
          name: "Decline ACL request for aivtopic1",
        }
      )
    ).toBeDisabled();
  });

  describe("user is unable to approve and decline non pending requests", () => {
    beforeEach(() => {
      renderFromProps({ aclRequests });
    });
    afterEach(cleanup);
    it("disables approve action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: /ACL approval requests/,
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const approve = within(approvedRequestRow).getByRole("button", {
        name: "Approve ACL request for newaudittopic",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is not in created state", async () => {
      const table = screen.getByRole("table", {
        name: /ACL approval requests/,
      });
      const rows = within(table).getAllByRole("row");
      const approvedRequestRow = rows[2];
      const decline = within(approvedRequestRow).getByRole("button", {
        name: "Decline ACL request for newaudittopic",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is unable to trigger action if some action is already in progress", () => {
    const isBeingApproved = jest.fn(() => true);
    const isBeingDeclined = jest.fn(() => true);
    beforeEach(() => {
      renderFromProps({ aclRequests, isBeingApproved, isBeingDeclined });
    });
    afterEach(cleanup);
    it("disables approve action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: /ACL approval requests/,
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const approve = within(createdRequestRow).getByRole("button", {
        name: "Approve ACL request for aivtopic1",
      });
      expect(approve).toBeDisabled();
    });
    it("disables decline action if request is already in progress", async () => {
      const table = screen.getByRole("table", {
        name: /ACL approval requests/,
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      const decline = within(createdRequestRow).getByRole("button", {
        name: "Decline ACL request for aivtopic1",
      });
      expect(decline).toBeDisabled();
    });
  });

  describe("user is able to view request details", () => {
    const onDetails = jest.fn();
    beforeEach(() => {
      renderFromProps({ aclRequests, onDetails });
    });
    afterAll(cleanup);
    it("triggers details action for the corresponding request when clicked", async () => {
      const table = screen.getByRole("table", {
        name: /ACL approval requests/,
      });
      const rows = within(table).getAllByRole("row");
      const createdRequestRow = rows[1];
      await userEvent.click(
        within(createdRequestRow).getByRole("button", {
          name: "View ACL request for aivtopic1",
        })
      );
      expect(onDetails).toHaveBeenCalledTimes(1);
      expect(onDetails).toHaveBeenCalledWith(1014);
    });
  });
});
