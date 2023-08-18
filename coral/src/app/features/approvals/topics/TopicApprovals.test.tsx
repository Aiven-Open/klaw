import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import TopicApprovals from "src/app/features/approvals/topics/TopicApprovals";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { getTeams } from "src/domain/team/team-api";
import { TopicRequest } from "src/domain/topic";
import {
  approveTopicRequest,
  declineTopicRequest,
  getTopicRequestsForApprover,
} from "src/domain/topic/topic-api";
import { transformGetTopicRequestsResponse } from "src/domain/topic/topic-transformer";
import { TopicRequestApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { approveSchemaRequest } from "src/domain/schema-request";

jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/team/team-api");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetTopicRequestsForApprover =
  getTopicRequestsForApprover as jest.MockedFunction<
    typeof getTopicRequestsForApprover
  >;
const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

const mockedTopicRequestsResponse: TopicRequest[] = [
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
    deletable: true,
    editable: true,
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
    deletable: true,
    editable: true,
    deleteAssociatedSchema: false,
  },
];

const mockedTeamsResponse = [
  {
    teamname: "Ospo",
    teammail: "ospo@aiven.io",
    teamphone: "003157843623",
    contactperson: "Ospo Office",
    tenantId: 101,
    teamId: 1003,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
  {
    teamname: "DevRel",
    teammail: "devrel@aiven.io",
    teamphone: "003146237478",
    contactperson: "Dev Rel",
    tenantId: 101,
    teamId: 1004,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
];

const mockedApiResponse: TopicRequestApiResponse =
  transformGetTopicRequestsResponse(mockedTopicRequestsResponse);
const mockGetEnvironmentResponse = transformEnvironmentApiResponse(
  mockedEnvironmentResponse
);

const mockApproveTopicRequest = approveTopicRequest as jest.MockedFunction<
  typeof approveSchemaRequest
>;
const mockDeclineTopicRequest = declineTopicRequest as jest.MockedFunction<
  typeof declineTopicRequest
>;

describe("TopicApprovals", () => {
  const defaultApiParams = {
    env: "ALL",
    pageNo: "1",
    requestStatus: "CREATED",
    search: "",
  };
  beforeAll(() => {
    mockIntersectionObserver();
  });
  afterAll(() => {
    jest.resetAllMocks();
  });

  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = jest.fn();
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTeams.mockResolvedValue([]);
    });
    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
    });

    it("shows a loading state instead of a table while topic requests are being fetched", () => {
      mockGetTopicRequestsForApprover.mockResolvedValue(
        transformGetTopicRequestsResponse([])
      );

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a error message in case of an error for fetching topic requests", async () => {
      mockGetTopicRequestsForApprover.mockRejectedValue(
        "Unexpected error. Please try again later!"
      );

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const skeleton = screen.getByTestId("skeleton-table");

      await waitForElementToBeRemoved(skeleton);

      const table = screen.queryByRole("table");
      const errorMessage = screen.getByText(
        "Unexpected error. Please try again later!"
      );

      expect(table).not.toBeInTheDocument();
      expect(errorMessage).toBeVisible();
      expect(console.error).toHaveBeenCalledWith(
        "Unexpected error. Please try again later!"
      );
    });
  });

  describe("renders all necessary elements ", () => {
    beforeAll(async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a select to filter by teams with default", () => {
      const select = screen.getByRole("combobox", { name: "Filter by team" });

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All teams");
    });

    it("shows a select to filter by environment with default", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All Environments");
    });

    it("shows a select to filter by status with default", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by status",
      });

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("Awaiting approval");
    });

    it("shows a search input to search for topic names", () => {
      const search = screen.getByRole("search", { name: "Search Topic name" });

      expect(search).toBeVisible();
    });

    it("shows a table with all topic requests and a header row", () => {
      const table = screen.getByRole("table", {
        name: "Topic approval requests, page 1 of 1",
      });
      const rows = within(table).getAllByRole("row");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(mockedApiResponse.entries.length + 1);
    });
  });

  describe("renders pagination dependent on response", () => {
    beforeEach(() => {
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 1,
        currentPage: 1,
        entries: [],
      });
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTeams.mockResolvedValue([]);
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = "/?page=100";
      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue({
        ...mockedApiResponse,
        totalPages: 1,
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });
      expect(pagination).not.toBeInTheDocument();
    });

    it("shows a pagination when response has more then one page", async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 2,
        currentPage: 1,
        entries: [],
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 2",
      });
      expect(pagination).toBeVisible();
    });

    it("shows the currently active page based on api response", async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 4",
      });
      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 1 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 1 of 3"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 2",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(2, {
        env: "ALL",
        pageNo: "2",
        requestStatus: "CREATED",
        search: "",
        teamId: undefined,
      });
    });
  });

  describe("shows a detail modal for Topic request", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows detail modal for first request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockedApiResponse.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${firstRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(firstRequest.topicname);
    });

    it("shows detail modal for last request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const lastRequest =
        mockedApiResponse.entries[mockedApiResponse.entries.length - 1];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${lastRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(lastRequest.topicname);
    });

    it("should render disabled actions in Details modal", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const approvedRequest = mockedApiResponse.entries[1];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${approvedRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

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
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const createdRequest = mockedApiResponse.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${createdRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

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
      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
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
      expect(screen.getByLabelText("Filter by team")).toBeVisible();
      expect(
        screen.getByRole("search", { name: "Search Topic name" })
      ).toBeVisible();
    });

    it("filters by Environment", async () => {
      const select = screen.getByLabelText("Filter by Environment");

      const devOption = within(select).getByRole("option", { name: "DEV" });

      expect(devOption).toBeEnabled();

      await userEvent.selectOptions(select, devOption);

      expect(select).toHaveDisplayValue("DEV");

      await waitFor(() => {
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
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
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          requestStatus: "DECLINED",
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
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          operationType: "CREATE",
        })
      );
    });

    it("filters by team", async () => {
      const select = screen.getByLabelText("Filter by team");

      const option = within(select).getByRole("option", {
        name: "Ospo",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("Ospo");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          teamId: 1003,
        })
      );
    });

    it("filters by Topic", async () => {
      const search = screen.getByRole("search", { name: "Search Topic name" });

      expect(search).toBeEnabled();

      await userEvent.type(search, "topicname");

      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          search: "topicname",
        })
      );
    });

    it("filters by several fields", async () => {
      const select = screen.getByLabelText("Filter by team");
      const option = within(select).getByRole("option", {
        name: "Ospo",
      });
      expect(option).toBeEnabled();
      await userEvent.selectOptions(select, option);
      expect(select).toHaveDisplayValue("Ospo");

      const search = screen.getByRole("search", { name: "Search Topic name" });
      expect(search).toBeEnabled();
      await userEvent.type(search, "topicname");
      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          ...defaultApiParams,
          search: "topicname",
          teamId: 1003,
        })
      );
    });
  });

  describe("enables user to approve a request with quick action", () => {
    const testRequest = mockedApiResponse.entries[0];

    const orignalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      console.error = orignalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("send a approve request api call if user approves a topic request", async () => {
      mockApproveTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const approveButton = screen.getByRole("button", {
        name: `Approve topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });
      expect(console.error).not.toHaveBeenCalled();
    });

    it("updates the the data for the table if user approves a topic request", async () => {
      mockApproveTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);
      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });

      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs user about error if approving request was not successful", async () => {
      mockApproveTopicRequest.mockRejectedValue("OH NO");
      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });

      expect(mockApproveTopicRequest).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();

      expect(console.error).toHaveBeenCalledWith("OH NO");
    });
  });

  describe("enables user to approve a request through details modal", () => {
    const testRequest = mockedApiResponse.entries[0];

    const orignalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      console.error = orignalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("send a approve request api call if user approves a topic request", async () => {
      mockApproveTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });
      expect(console.error).not.toHaveBeenCalled();
      expect(modal).not.toBeInTheDocument();
    });

    it("updates the the data for the table if user approves a topic request", async () => {
      mockApproveTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);
      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });

      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
      expect(modal).not.toBeInTheDocument();
    });

    it("informs user about error if approving request was not successful", async () => {
      mockApproveTopicRequest.mockRejectedValue("OH NO");
      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${testRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog", { name: "Request details" });

      expect(modal).toBeVisible();
      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });

      await userEvent.click(approveButton);

      expect(mockApproveTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
      });

      expect(mockApproveTopicRequest).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();
      expect(modal).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalledWith("OH NO");
    });
  });

  describe("enables user to decline a request with quick action", () => {
    const testRequest = mockedApiResponse.entries[0];

    const orignalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      console.error = orignalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("does not send a decline request is user does not add a reason", async () => {
      mockDeclineTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const declineButton = screen.getByRole("button", {
        name: `Decline topic request for ${testRequest.topicname}`,
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

      expect(mockDeclineTopicRequest).not.toHaveBeenCalled();
      expect(declineModal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("send a decline request api call if user declines a topic request", async () => {
      mockDeclineTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const declineButton = screen.getByRole("button", {
        name: `Decline topic request for ${testRequest.topicname}`,
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

      expect(mockDeclineTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
        reason: "my reason",
      });

      expect(console.error).not.toHaveBeenCalled();
      expect(declineModal).not.toBeInTheDocument();
    });

    it("updates the the data for the table if user declines a topic request", async () => {
      mockDeclineTopicRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const declineButton = screen.getByRole("button", {
        name: `Decline topic request for ${testRequest.topicname}`,
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

      expect(mockDeclineTopicRequest).toHaveBeenCalledWith({
        reqIds: [String(testRequest.topicid)],
        reason: "my reason",
      });

      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
      expect(console.error).not.toHaveBeenCalled();
    });

    it("informs user about error if declining request was not successful", async () => {
      mockDeclineTopicRequest.mockRejectedValue("Oh no");

      const declineButton = screen.getByRole("button", {
        name: `Decline topic request for ${testRequest.topicname}`,
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

      expect(mockApproveTopicRequest).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();
      expect(declineModal).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalledWith("Oh no");
    });
  });

  describe("enables user to decline a request through details modal", () => {
    const testRequest = mockedApiResponse.entries[0];

    const orignalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();

      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTeams.mockResolvedValue(mockedTeamsResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      console.error = orignalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("opens the decline user flow when user clicks decline in details modal", async () => {
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${testRequest.topicname}`,
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
