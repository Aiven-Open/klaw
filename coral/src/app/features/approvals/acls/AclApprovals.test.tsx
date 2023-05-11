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
import {
  Environment,
  getAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { vi } from "vitest";

vi.mock("src/domain/acl/acl-api.ts");
vi.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as vi.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetAclRequestsForApprover =
  getAclRequestsForApprover as vi.MockedFunction<
    typeof getAclRequestsForApprover
  >;

const mockedAclRequestsForApproverApiResponse: AclRequest[] = [
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
    requestOperationType: "CREATE",
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
    requestingTeamName: "Ospo",
    appname: "App",
    requestor: "amathieu",
    requestOperationType: "CREATE",
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
    mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
  });
  afterAll(() => {
    vi.resetAllMocks();
  });

  describe("shows loading or error state for fetching acls requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = vi.fn();
      mockGetEnvironments.mockResolvedValue([]);
    });
    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
    });

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
      expect(console.error).not.toHaveBeenCalled();
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
      expect(console.error).toHaveBeenCalledWith(
        "Unexpected error. Please try again later!"
      );
    });
  });

  describe("handles paginated data", () => {
    beforeAll(async () => {
      mockGetEnvironments.mockResolvedValue([]);
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
      vi.resetAllMocks();
    });

    it("render a Pagination component", async () => {
      const pagination = screen.getByRole("navigation");
      expect(pagination).toBeVisible();
    });

    it("render Pagination on page 1 on load", async () => {
      const pagination = screen.getByRole("navigation");
      expect(pagination).toHaveTextContent("Page 1 of 2");
    });

    it("should render disabled actions in Details modal", async () => {
      const approvedRow = screen.getAllByRole("row")[2];
      await userEvent.click(
        within(approvedRow).getByRole("button", {
          name: /View acl request for/,
        })
      );
      const modal = screen.getByRole("dialog");
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });
      const declineButton = within(modal).getByRole("button", {
        name: "Decline",
      });

      expect(approveButton).toBeDisabled();
      expect(declineButton).toBeDisabled();
    });

    it("should render enabled actions in Details modal", async () => {
      const createdRow = screen.getAllByRole("row")[1];

      await userEvent.click(
        within(createdRow).getByRole("button", {
          name: /View acl request for/,
        })
      );

      const modal = screen.getByRole("dialog");
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });
      const declineButton = within(modal).getByRole("button", {
        name: "Decline",
      });

      expect(approveButton).toBeEnabled();
      expect(declineButton).toBeEnabled();
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
      vi.resetAllMocks();
      cleanup();
    });

    it("renders a select to filter by environment with default", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All Environments");
    });

    it("renders a select to filter by status with default", () => {
      const select = screen.getByRole("combobox", { name: "Filter by status" });

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("Awaiting approval");
    });

    it("renders a select to filter by ACL type with default", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by ACL type",
      });
      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All ACL types");
    });

    it("renders a select to filter by request type with default", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by request type",
      });
      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All request types");
    });

    it("renders a search field for topic names", () => {
      const search = screen.getByRole("search", { name: "Search Topic name" });

      expect(search).toBeVisible();
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
          search: "",
        });
      });
    });

    it("filters by Status", async () => {
      const select = screen.getByLabelText("Filter by status");

      const option = within(select).getByRole("option", {
        name: "Declined",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("Declined");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "DECLINED",
          search: "",
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
          search: "",
        })
      );
    });

    it("filters by Topic", async () => {
      const search = screen.getByRole("search", { name: "Search Topic name" });

      expect(search).toBeEnabled();

      await userEvent.type(search, "topicname");

      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "topicname",
        })
      );
    });

    it("filters by Request type", async () => {
      const select = screen.getByLabelText("Filter by request type");

      const option = within(select).getByRole("option", {
        name: "Create",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("Create");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "",
          operationType: "CREATE",
        })
      );
    });

    it("filters by several fields", async () => {
      const select = screen.getByLabelText("Filter by ACL type");
      const option = within(select).getByRole("option", {
        name: "PRODUCER",
      });
      expect(option).toBeEnabled();
      await userEvent.selectOptions(select, option);
      expect(select).toHaveDisplayValue("PRODUCER");

      const search = screen.getByRole("search", { name: "Search Topic name" });
      expect(search).toBeEnabled();
      await userEvent.type(search, "topicname");
      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "PRODUCER",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "topicname",
        })
      );
    });
  });

  describe("handles default filtering from URL search params", () => {
    beforeEach(async () => {
      mockGetAclRequestsForApprover.mockResolvedValue(
        mockGetAclRequestsForApproverResponse
      );
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);

      customRender(<AclApprovals />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?requestType=DELETE",
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      vi.resetAllMocks();
      cleanup();
    });

    it("filters from value in URL search params", async () => {
      const select = screen.getByLabelText("Filter by request type");

      expect(select).toHaveValue("DELETE");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          aclType: "ALL",
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "",
          operationType: "DELETE",
        })
      );
    });
  });
});
