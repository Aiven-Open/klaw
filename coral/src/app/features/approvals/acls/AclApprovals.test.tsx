import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AclApprovals from "src/app/features/approvals/acls/AclApprovals";
import {
  approveAclRequest,
  declineAclRequest,
  getAclRequestsForApprover,
} from "src/domain/acl/acl-api";
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

jest.mock("src/domain/acl/acl-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetAclRequestsForApprover =
  getAclRequestsForApprover as jest.MockedFunction<
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

const mockDeclineAclRequest = declineAclRequest as jest.MockedFunction<
  typeof declineAclRequest
>;
const mockApproveAclRequest = approveAclRequest as jest.MockedFunction<
  typeof approveAclRequest
>;

describe("AclApprovals", () => {
  const defaultApiParams = {
    aclType: "ALL",
    env: "ALL",
    pageNo: "1",
    requestStatus: "CREATED",
    search: "",
  };

  beforeAll(() => {
    mockIntersectionObserver();
    mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
  });
  afterAll(() => {
    jest.resetAllMocks();
  });

  describe("shows loading or error state for fetching acls requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = jest.fn();
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

    it("should render disabled actions in Details modal", async () => {
      const approvedRow = screen.getAllByRole("row")[2];
      await userEvent.click(
        within(approvedRow).getByRole("button", {
          name: /View ACL request for/,
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
          name: /View ACL request for/,
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
      jest.resetAllMocks();
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
          ...defaultApiParams,
          env: "1",
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
          ...defaultApiParams,
          requestStatus: "DECLINED",
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
          ...defaultApiParams,
          aclType: "PRODUCER",
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
          ...defaultApiParams,
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
          ...defaultApiParams,
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
          ...defaultApiParams,
          aclType: "PRODUCER",
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
      jest.resetAllMocks();
      cleanup();
    });

    it("filters from value in URL search params", async () => {
      const select = screen.getByLabelText("Filter by request type");

      expect(select).toHaveValue("DELETE");

      await waitFor(() =>
        expect(mockGetAclRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          operationType: "DELETE",
        })
      );
    });
  });

  describe("enables user to approve a request with quick action", () => {
    const testRequest = mockGetAclRequestsForApproverResponse.entries[0];

    const originalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

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
      console.error = originalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("send a approve request api call if user approves a acl request", async () => {
      mockApproveAclRequest.mockResolvedValue([{ success: true, message: "" }]);

      const approveButton = screen.getByRole("button", {
        name: `Approve ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("updates the the data for the table if user approves a acl request", async () => {
      mockApproveAclRequest.mockResolvedValue([{ success: true, message: "" }]);
      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });

      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs user about error if approving request was not successful", async () => {
      mockApproveAclRequest.mockRejectedValue("OH NO");
      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });

      expect(mockGetAclRequestsForApprover).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();

      expect(console.error).toHaveBeenCalledWith("OH NO");
    });
  });

  describe("enables user to approve a request through details modal", () => {
    const testRequest = mockGetAclRequestsForApproverResponse.entries[0];

    const originalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      console.error = jest.fn();
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
      console.error = originalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("send a approve request api call if user approves a acl request", async () => {
      mockApproveAclRequest.mockResolvedValue([{ success: true, message: "" }]);

      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });
      expect(console.error).not.toHaveBeenCalled();
      expect(modal).not.toBeInTheDocument();
    });

    it("updates the the data for the table if user approves a acl request", async () => {
      mockApproveAclRequest.mockResolvedValue([{ success: true, message: "" }]);
      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });

      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
      expect(modal).not.toBeInTheDocument();
    });

    it("informs user about error if approving request was not successful", async () => {
      mockApproveAclRequest.mockRejectedValue("OH NO");
      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
      });

      expect(mockGetAclRequestsForApprover).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();
      expect(modal).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalledWith("OH NO");
    });
  });

  describe("enables user to decline a request with quick action", () => {
    const testRequest = mockGetAclRequestsForApproverResponse.entries[0];

    const originalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      console.error = jest.fn();
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
      console.error = originalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("does not send a decline request is user does not add a reason", async () => {
      mockDeclineAclRequest.mockResolvedValue([{ success: true, message: "" }]);

      const declineButton = screen.getByRole("button", {
        name: `Decline ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);

      const declineModal = screen.getByRole("dialog", {
        name: "Decline request",
      });
      expect(declineModal).toBeVisible();

      const confirmDecline = within(declineModal).getByRole("button", {
        name: "Decline request",
      });

      expect(confirmDecline).toBeDisabled();
      await userEvent.click(confirmDecline);

      expect(mockDeclineAclRequest).not.toHaveBeenCalled();
      expect(declineModal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("send a decline request api call if user declines a ACL request", async () => {
      mockDeclineAclRequest.mockResolvedValue([{ success: true, message: "" }]);

      const declineButton = screen.getByRole("button", {
        name: `Decline ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);

      const declineModal = screen.getByRole("dialog", {
        name: "Decline request",
      });
      expect(declineModal).toBeVisible();

      const textAreaReason = within(declineModal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(textAreaReason, "my reason");

      const confirmDecline = within(declineModal).getByRole("button", {
        name: "Decline request",
      });

      await userEvent.click(confirmDecline);

      expect(mockDeclineAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
        reason: "my reason",
      });

      expect(console.error).not.toHaveBeenCalled();
      expect(declineModal).not.toBeInTheDocument();
    });

    it("updates the the data for the table if user declines a ACL request", async () => {
      mockDeclineAclRequest.mockResolvedValue([{ success: true, message: "" }]);

      const declineButton = screen.getByRole("button", {
        name: `Decline ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);

      const declineModal = screen.getByRole("dialog", {
        name: "Decline request",
      });
      expect(declineModal).toBeVisible();

      const textAreaReason = within(declineModal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(textAreaReason, "my reason");

      const confirmDecline = within(declineModal).getByRole("button", {
        name: "Decline request",
      });

      await userEvent.click(confirmDecline);

      expect(mockDeclineAclRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.req_no)],
        reason: "my reason",
      });

      expect(mockGetAclRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs user about error if declining request was not successful", async () => {
      mockDeclineAclRequest.mockRejectedValue("Oh no");

      const declineButton = screen.getByRole("button", {
        name: `Decline ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);

      const declineModal = screen.getByRole("dialog", {
        name: "Decline request",
      });
      expect(declineModal).toBeVisible();

      const textAreaReason = within(declineModal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(textAreaReason, "my reason");

      const confirmDecline = within(declineModal).getByRole("button", {
        name: "Decline request",
      });

      await userEvent.click(confirmDecline);

      expect(mockGetAclRequestsForApprover).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();
      expect(declineModal).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalledWith("Oh no");
    });
  });

  describe("enables user to decline a request through details modal", () => {
    const testRequest = mockGetAclRequestsForApproverResponse.entries[0];

    const originalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      console.error = jest.fn();
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
      console.error = originalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("opens the decline user flow when user clicks decline in details modal", async () => {
      const viewDetailsButton = screen.getByRole("button", {
        name: `View ACL request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const detailsModal = screen.getByRole("dialog", {
        name: "Request details",
      });

      expect(detailsModal).toBeVisible();
      const declineButton = within(detailsModal).getByRole("button", {
        name: "Decline",
      });

      await userEvent.click(declineButton);

      expect(detailsModal).not.toBeInTheDocument();

      const declineModal = screen.getByRole("dialog", {
        name: "Decline request",
      });

      expect(declineModal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });
  });
});
