import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import TopicApprovals from "src/app/features/approvals/topics/TopicApprovals";
import { getEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { getTeams } from "src/domain/team/team-api";
import { TopicRequest } from "src/domain/topic";
import { getTopicRequestsForApprover } from "src/domain/topic/topic-api";
import { transformGetTopicRequestsResponse } from "src/domain/topic/topic-transformer";
import { TopicRequestApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/team/team-api");

const mockGetEnvironments = getEnvironments as jest.MockedFunction<
  typeof getEnvironments
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

describe("TopicApprovals", () => {
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
      const search = screen.getByRole("search");

      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Search for an exact match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
    });

    it("shows a table with all topic requests", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("rowgroup");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(mockedApiResponse.entries.length);
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
        env: "ALL",
        pageNo: "100",
        requestStatus: "CREATED",
        search: "",
        teamId: undefined,
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
        env: "ALL",
        pageNo: "1",
        requestStatus: "CREATED",
        search: "",
        teamId: undefined,
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
      const modal = screen.getByRole("dialog");

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
      const modal = screen.getByRole("dialog");

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
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const createdRequest = mockedApiResponse.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View topic request for ${createdRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
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
      expect(screen.getByRole("search")).toBeVisible();
    });

    it("filters by Environment", async () => {
      const select = screen.getByLabelText("Filter by Environment");

      const devOption = within(select).getByRole("option", { name: "DEV" });

      expect(devOption).toBeEnabled();

      await userEvent.selectOptions(select, devOption);

      expect(select).toHaveDisplayValue("DEV");

      await waitFor(() => {
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          env: "1",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "",
          teamId: undefined,
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
          env: "ALL",
          pageNo: "1",
          requestStatus: "DECLINED",
          search: "",
          teamId: undefined,
        })
      );
    });

    it("filters by ACL type", async () => {
      const select = screen.getByLabelText("Filter by team");

      const option = within(select).getByRole("option", {
        name: "Ospo",
      });

      expect(option).toBeEnabled();

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue("Ospo");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "",
          teamId: 1003,
        })
      );
    });

    it("filters by Topic", async () => {
      const search = screen.getByRole("search");

      expect(search).toBeEnabled();

      await userEvent.type(search, "topicname");

      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "topicname",
          teamId: undefined,
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

      const search = screen.getByRole("search");
      expect(search).toBeEnabled();
      await userEvent.type(search, "topicname");
      expect(search).toHaveValue("topicname");

      await waitFor(() =>
        expect(mockGetTopicRequestsForApprover).toHaveBeenCalledWith({
          env: "ALL",
          pageNo: "1",
          requestStatus: "CREATED",
          search: "topicname",
          teamId: 1003,
        })
      );
    });
  });
});
