import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AclApprovals from "src/app/features/approvals/acls/AclApprovals";
import { getAclRequestsForApprover } from "src/domain/acl/acl-api";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import { AclRequest } from "src/domain/acl/acl-types";
import { Environment, getEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironments = getEnvironments as jest.MockedFunction<
  typeof getEnvironments
>;

const mockGetAclRequestsForApprover =
  getAclRequestsForApprover as jest.MockedFunction<
    typeof getAclRequestsForApprover
  >;

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
    requestStatus: "CREATED",
    approver: undefined,
    approvingtime: undefined,
    aclResourceType: undefined,
    currentPage: "1",
    otherParams: undefined,
    totalNoPages: "2",
    allPageNos: ["1", ">", ">>"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
  },
];
const mockGetEnvironmentResponse: Environment[] =
  transformEnvironmentApiResponse(mockedEnvironmentResponse);

const mockGetAclRequestsForApproverResponse = transformAclRequestApiResponse(
  mockedAclRequestsForApproverApiResponse
);
const mockGetAclRequestsForApproverResponseEmpty =
  transformAclRequestApiResponse([]);

describe("AclApprovals", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });
  afterAll(() => {
    jest.resetAllMocks();
  });

  describe("shows loading or error state for fetching acls requests", () => {
    afterEach(cleanup);

    it("shows a skeleton table while loading", () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponseEmpty
      );
      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });
      const skeleton = screen.getByTestId("skeleton-table");

      expect(skeleton).toBeVisible();
    });

    it("shows an error message when an error occurs", async () => {
      mockGetAclRequestsForApprover.mockRejectedValue(
        "Unexpected error. Please try again later!"
      );

      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const skeleton = screen.getByTestId("skeleton-table");

      await waitForElementToBeRemoved(skeleton);

      const error = screen.getByText(
        "Unexpected error. Please try again later!"
      );

      expect(error).toBeVisible();
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
      jest.resetAllMocks();
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
          return cell.textContent === "mbasani maulbach ";
        })
      ).toHaveLength(1);
      expect(
        cells.filter((cell) => cell.textContent === "3.3.3.32 3.3.3.33 ")
      ).toHaveLength(1);
    });
  });

  describe("handles paginated data", () => {
    beforeAll(async () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponse
      );

      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("render a Pagination component", async () => {
      const pagination = screen.getByRole("navigation");
      expect(pagination).toBeVisible();
    });

    it("render Pagination on page 1 on load", async () => {
      const pagination = screen.getByRole("navigation");
      expect(pagination).toHaveTextContent("Page 1 of 2");
    });
  });

  describe("handles filtering", () => {
    beforeEach(async () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponse
      );
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);

      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("renders correct filters", () => {
      expect(screen.getByLabelText("Filter by Environment")).toBeVisible();
      expect(screen.getByLabelText("Filter by status")).toBeVisible();
      expect(screen.getByLabelText("Filter by ACL type")).toBeVisible();
      expect(screen.getByRole("search")).toBeVisible();
    });

    it("filters by Environment", async () => {
      const select = screen.getByLabelText("Filter by Environment");

      const devOption = within(select).getByRole("option", { name: "DEV" });

      expect(devOption).toBeEnabled();

      await userEvent.selectOptions(select, devOption);

      expect(select).toHaveDisplayValue("DEV");

      await waitFor(() => {
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "1",
          pageNo: "1",
          requestStatus: "CREATED",
          topic: "",
        });
      });
    });

    it("filters by Status", async () => {
      const select = screen.getByLabelText("Filter by status");

      const option = within(select).getByRole("option", {
        name: "DECLINED",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("DECLINED");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "DECLINED",
          topic: "",
        })
      );
    });

    it("filters by ACL type", async () => {
      const select = screen.getByLabelText("Filter by ACL type");

      const option = within(select).getByRole("option", {
        name: "PRODUCER",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("PRODUCER");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "PRODUCER",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          topic: "",
        })
      );
    });

    it("filters by Topic", async () => {
      const search = screen.getByRole("search");

      expect(search).toBeEnabled();

      await userEvent.type(search, "topicname");

      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          topic: "topicname",
        })
      );
    });

    it("filters by several fields", async () => {
      const search = screen.getByRole("search");
      expect(search).toBeEnabled();
      await userEvent.type(search, "topicname");
      expect(search).toHaveValue("topicname");

      const select = screen.getByLabelText("Filter by ACL type");
      const option = within(select).getByRole("option", {
        name: "PRODUCER",
      });
      expect(option).toBeEnabled();
      await userEvent.selectOptions(select, option);
      expect(select).toHaveDisplayValue("PRODUCER");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "PRODUCER",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          topic: "topicname",
        })
      );
    });
  });
});
