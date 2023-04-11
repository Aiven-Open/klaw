import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import ConnectorApprovals from "src/app/features/approvals/connectors/ConnectorApprovals";
import {
  ConnectorRequest,
  getConnectorRequestsForApprover,
} from "src/domain/connector";
import { transformConnectorRequestApiResponse } from "src/domain/connector/connector-transformer";
import { getSyncConnectorsEnvironments } from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/connector/connector-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetSyncConnectorsEnvironments =
  getSyncConnectorsEnvironments as jest.MockedFunction<
    typeof getSyncConnectorsEnvironments
  >;

const mockGetConnectorRequestsForApprover =
  getConnectorRequestsForApprover as jest.MockedFunction<
    typeof getConnectorRequestsForApprover
  >;

const mockedEnvironments = [
  {
    id: "4",
    name: "DEV",
    type: "kafkaconnect",
  },
  {
    id: "4",
    name: "TST",
    type: "kafkaconnect",
  },
];

const mockedEnvironmentResponse = transformEnvironmentApiResponse([
  createMockEnvironmentDTO(mockedEnvironments[0]),
  createMockEnvironmentDTO(mockedEnvironments[1]),
]);

const mockedConnectorRequests: ConnectorRequest[] = [
  {
    environment: "4",
    environmentName: "DEV",
    requestor: "aindriul",
    teamId: 1003,
    teamname: "Ospo",
    requestOperationType: "DELETE",
    requestStatus: "CREATED",
    requesttime: "2023-04-06T08:04:39.783+00:00",
    requesttimestring: "06-Apr-2023 08:04:39",
    currentPage: "1",
    totalNoPages: "1",
    allPageNos: ["1"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,amathieu,roopek,miketest,harshini,mischa,",
    connectorName: "Mirjam-10",
    description: "Mirjam-10",
    connectorConfig:
      '{\n  "name" : "Mirjam-10",\n  "topic" : "testtopic",\n  "tasks.max" : "1",\n  "topics.regex" : "*",\n  "connector.class" : "io.confluent.connect.storage.tools.ConnectorSourceConnector"\n}',
    connectorId: 1026,
    deletable: false,
    editable: false,
  },
  {
    environment: "4",
    environmentName: "DEV",
    requestor: "miketest",
    teamId: 1003,
    teamname: "Ospo",
    requestOperationType: "CREATE",
    requestStatus: "CREATED",
    requesttime: "2023-04-04T13:12:32.970+00:00",
    requesttimestring: "04-Apr-2023 13:12:32",
    currentPage: "1",
    totalNoPages: "1",
    allPageNos: ["1"],
    approvingTeamDetails:
      "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,amathieu,aindriul,roopek,harshini,mischa,",
    connectorName: "Mirjam-7",
    description: "Mirjam-7",
    connectorConfig:
      '{\n  "name" : "Mirjam-2",\n  "topic" : "testtopic",\n  "tasks.max" : "1",\n  "topics.regex" : "*",\n  "connector.class" : "io.confluent.connect.storage.tools.ConnectorSourceConnector"\n}',
    connectorId: 1010,
    deletable: false,
    editable: false,
  },
];

const mockedApiResponseConnectorRequests = transformConnectorRequestApiResponse(
  mockedConnectorRequests
);

describe("ConnectorApprovals", () => {
  const defaultApiParams = {
    pageNo: "1",
    requestStatus: "CREATED",
    env: "ALL",
    search: "",
  };
  beforeAll(() => {
    mockIntersectionObserver();
  });

  //@TODO - I remove the useQuery mock because that isn't useful for a component
  // using more then one query. While this block still covers the cases, it's
  // more brittle due to it's dependency on the async process of the api call
  // I'll add a helper for controlling api mocks better (get a loading state etc)
  // after this release cycle.
  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      // used to swallow a console.error that _should_ happen
      // while making sure to not swallow other console.errors
      console.error = jest.fn();

      mockGetConnectorRequestsForApprover.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a loading state instead of a table while schema requests are being fetched", () => {
      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a error message in case of an error for fetching schema requests", async () => {
      mockGetConnectorRequestsForApprover.mockRejectedValue("mock-error");

      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const errorMessage = await screen.findByText(
        "Unexpected error. Please try again later!"
      );

      expect(table).not.toBeInTheDocument();
      expect(errorMessage).toBeVisible();
      expect(console.error).toHaveBeenCalledWith("mock-error");
    });
  });

  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      mockGetSyncConnectorsEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequestsForApprover.mockResolvedValue(
        mockedApiResponseConnectorRequests
      );

      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a select to filter by environment with default", () => {
      const select = screen.getByLabelText("Filter by Environment");

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All Environments");
    });

    it("shows a select to filter by status with default value", () => {
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
        'Search for an partial match in name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
    });

    it("shows a table with all schema requests and a header row", () => {
      const table = screen.getByRole("table", {
        name: "Connector approval requests, page 1 of 1",
      });
      const rows = within(table).getAllByRole("row");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(
        mockedApiResponseConnectorRequests.entries.length + 1
      );
    });
  });

  describe("renders pagination dependent on response", () => {
    beforeEach(() => {
      mockGetConnectorRequestsForApprover.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = `/?page=100`;
      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetConnectorRequestsForApprover.mockResolvedValue({
        ...mockedApiResponseConnectorRequests,
        totalPages: 1,
      });

      customRender(<ConnectorApprovals />, {
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
      mockGetConnectorRequestsForApprover.mockResolvedValue({
        totalPages: 4,
        currentPage: 1,
        entries: [],
      });

      customRender(<ConnectorApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 4",
      });
      expect(pagination).toBeVisible();
    });

    it("shows the currently active page based on api response", async () => {
      mockGetConnectorRequestsForApprover.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<ConnectorApprovals />, {
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
      mockGetConnectorRequestsForApprover.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(<ConnectorApprovals />, {
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

      expect(mockGetConnectorRequestsForApprover).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        pageNo: "2",
      });
    });
  });
});
