import { cleanup, screen, waitFor, within } from "@testing-library/react";
import {
  getSchemaRequestsForApprover,
  SchemaRequest,
} from "src/domain/schema-request";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";
import { transformGetSchemaRequestsForApproverResponse } from "src/domain/schema-request/schema-request-transformer";
import SchemaApprovals from "src/app/features/approvals/schemas/SchemaApprovals";
import { getSchemaRegistryEnvironments } from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";

jest.mock("src/domain/schema-request/schema-request-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetSchemaRegistryEnvironments =
  getSchemaRegistryEnvironments as jest.MockedFunction<
    typeof getSchemaRegistryEnvironments
  >;

const mockGetSchemaRequestsForApprover =
  getSchemaRequestsForApprover as jest.MockedFunction<
    typeof getSchemaRequestsForApprover
  >;

const mockedEnvironments = [
  { name: "DEV", id: "1" },
  { name: "TST", id: "2" },
];

const mockedEnvironmentResponse = transformEnvironmentApiResponse([
  createMockEnvironmentDTO(mockedEnvironments[0]),
  createMockEnvironmentDTO(mockedEnvironments[1]),
]);

const mockedResponseSchemaRequests: SchemaRequest[] = [
  {
    req_no: 1014,
    topicname: "testtopic-first",
    environment: "1",
    environmentName: "BRG",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    requestStatus: "CREATED",
    requestOperationType: "CREATE",
    remarks: "asap",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    approvingtime: "2022-11-04T14:54:13.414+00:00",
    totalNoPages: "4",
    allPageNos: ["1"],
    currentPage: "1",
    deletable: false,
    editable: false,
    forceRegister: false,
  },
  {
    req_no: 1013,
    topicname: "testtopic-second",
    environment: "2",
    environmentName: "SEC",
    schemaversion: "1.0",
    teamname: "NCC1701D",
    teamId: 1701,
    appname: "App",
    schemafull: "",
    username: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    requestStatus: "CREATED",
    requestOperationType: "DELETE",
    remarks: "asap",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
    approvingtime: "2022-11-04T14:54:13.414+00:00",
    totalNoPages: "4",
    allPageNos: ["1"],
    currentPage: "1",
    deletable: false,
    editable: false,
    forceRegister: false,
  },
];

const mockedApiResponseSchemaRequests: SchemaRequestApiResponse =
  transformGetSchemaRequestsForApproverResponse(mockedResponseSchemaRequests);

describe("SchemaApprovals", () => {
  const defaultApiParams = {
    pageNo: "1",
    requestStatus: "CREATED",
    env: "ALL",
    topic: "",
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

      mockGetSchemaRequestsForApprover.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a loading state instead of a table while schema requests are being fetched", () => {
      customRender(<SchemaApprovals />, {
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
      mockGetSchemaRequestsForApprover.mockRejectedValue("mock-error");

      customRender(<SchemaApprovals />, {
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
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequestsForApprover.mockResolvedValue(
        mockedApiResponseSchemaRequests
      );

      customRender(<SchemaApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a select to filter by environment", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      expect(select).toBeVisible();
    });

    it("shows a select to filter by status", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by status",
      });

      expect(select).toBeVisible();
    });

    it("shows a search input to search for topic names", () => {
      const search = screen.getByRole("search");

      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Press "Enter" to start your search. Press "Escape" to delete all your input.'
      );
    });

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const rows = within(table).getAllByRole("rowgroup");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(mockedApiResponseSchemaRequests.entries.length);
    });
  });

  describe("renders pagination dependent on response", () => {
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = `/?page=100`;
      customRender(<SchemaApprovals />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetSchemaRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<SchemaApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetSchemaRequestsForApprover).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        ...mockedApiResponseSchemaRequests,
        totalPages: 1,
      });

      customRender(<SchemaApprovals />, {
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
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        totalPages: 4,
        currentPage: 1,
        entries: [],
      });

      customRender(<SchemaApprovals />, {
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
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<SchemaApprovals />, {
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
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<SchemaApprovals />, {
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

      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        pageNo: "2",
      });
    });
  });

  describe("shows a detail modal for schema request", () => {
    beforeEach(async () => {
      mockGetSchemaRequestsForApprover.mockResolvedValue(mockedApiResponse);

      customRender(<SchemaApprovals />, {
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
        name: `View schema request for ${firstRequest.topicname}`,
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
        name: `View schema request for ${lastRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(lastRequest.topicname);
    });
  });

  describe("handles filtering entries in the table", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        totalPages: 1,
        currentPage: 1,
        entries: mockedResponseSchemaRequests,
      });

      customRender(<SchemaApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("enables user to filter by 'status'", async () => {
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const statusFilter = screen.getByRole("combobox", {
        name: "Filter by status",
      });
      const statusOption = screen.getByRole("option", { name: "All statuses" });
      await userEvent.selectOptions(statusFilter, statusOption);

      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        requestStatus: "ALL",
      });
    });

    it("enables user to filter by 'environment'", async () => {
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const statusFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });
      const statusOption = screen.getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      await userEvent.selectOptions(statusFilter, statusOption);

      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        env: mockedEnvironments[0].id,
      });
    });

    it("enables user to search for topic", async () => {
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const search = screen.getByRole("search");

      await userEvent.type(search, "myTopic");

      await waitFor(() => {
        expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(2, {
          ...defaultApiParams,
          topic: "myTopic",
        });
      });
    });
  });
});
