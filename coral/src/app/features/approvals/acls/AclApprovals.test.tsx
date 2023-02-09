import * as ReactQuery from "@tanstack/react-query";
import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import AclApprovals from "src/app/features/approvals/acls/AclApprovals";
import { getAclRequestsForApprover } from "src/domain/acl/acl-api";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import { AclRequest } from "src/domain/acl/acl-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

jest.mock("src/domain/acl/acl-api.ts");

const mockGetAclRequestsForApprover =
  getAclRequestsForApprover as jest.MockedFunction<
    typeof getAclRequestsForApprover
  >;

const useQuerySpy = jest.spyOn(ReactQuery, "useQuery");

const mockedAclRequestsForApproverApiResponse: AclRequest[] = [
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
    topictype: "Consumer",
    aclIpPrincipleType: "PRINCIPAL",
    environmentName: "DEV",
    teamId: 1003,
    requestingteam: 1003,
    appname: "App",
    username: "amathieu",
    requesttime: "2023-01-06T14:50:37.912+00:00",
    requesttimestring: "06-Jan-2023 14:50:37",
    aclstatus: "created",
    approver: undefined,
    approvingtime: undefined,
    aclType: "Consumer",
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "1",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
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
    topictype: "Producer",
    aclIpPrincipleType: "IP_ADDRESS",
    environmentName: "TST",
    teamId: 1003,
    requestingteam: 1003,
    appname: "App",
    username: "amathieu",
    requesttime: "2023-01-10T13:19:10.757+00:00",
    requesttimestring: "10-Jan-2023 13:19:10",
    aclstatus: "created",
    approver: undefined,
    approvingtime: undefined,
    aclType: "Producer",
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "1",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
  },
];

const mockGetAclRequestsForApproverResponse = transformAclRequestApiResponse(
  mockedAclRequestsForApproverApiResponse
);

describe("AclApprovals", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });
  afterEach(() => {
    jest.resetAllMocks();
  });

  describe("Skeleton", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: [], isLoading: true });

      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterAll(() => {
      cleanup();
      useQuerySpy.mockRestore();
    });

    it("shows a skeleton table", () => {
      const skeleton = screen.getByTestId("skeleton-table");

      expect(skeleton).toBeVisible();
    });
  });

  describe("DataTable", () => {
    beforeAll(async () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponse
      );
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
    });

    it("shows one header row and two data rows", async () => {
      const rows = screen.getAllByRole("row");

      expect(rows).toHaveLength(3);
      expect(rows[0]).toHaveTextContent("Principals/Usernames");
      expect(rows[1]).toHaveTextContent("mbasani");
      expect(rows[2]).toHaveTextContent("User:*");
    });

    it("renders (prefixed) in Topic cell when appropriate", async () => {
      const prefixedCells = screen.getAllByText("(prefixed)");
      const notPrefixedCells = screen.getAllByText("aivtopic1");

      expect(prefixedCells).toHaveLength(1);
      expect(notPrefixedCells).toHaveLength(1);
    });

    it("renders all values for cells who can have multiple values", async () => {
      const cells = screen.getAllByRole("cell");

      expect(
        cells.filter((cell) => {
          return cell.textContent === "mbasanimaulbach";
        })
      ).toHaveLength(1);
      expect(
        cells.filter((cell) => cell.textContent === "3.3.3.323.3.3.33")
      ).toHaveLength(1);
    });
  });

  describe("Pagination", () => {
    beforeAll(async () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponse
      );
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
    });

    it("render a Pagination component", async () => {
      const pagination = screen.getByRole("navigation");

      expect(pagination).toBeInTheDocument();
      expect(pagination).toBeEnabled();
    });

    it("render Pagination on page 1 on load", async () => {
      const pagination = screen.getByRole("navigation");
      expect(pagination).toHaveTextContent("Page 1 of 1");
    });
  });
});
