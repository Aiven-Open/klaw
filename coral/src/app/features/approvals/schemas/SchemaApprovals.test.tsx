import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import SchemaApprovals from "src/app/features/approvals/schemas/SchemaApprovals";
import { getSchemaRegistryEnvironments } from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import {
  getSchemaRequestsForApprover,
  SchemaRequest,
  approveSchemaRequest,
  declineSchemaRequest,
} from "src/domain/schema-request";
import { transformGetSchemaRequests } from "src/domain/schema-request/schema-request-transformer";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

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

const mockDeclineSchemaRequest = declineSchemaRequest as jest.MockedFunction<
  typeof declineSchemaRequest
>;
const mockApproveSchemaRequest = approveSchemaRequest as jest.MockedFunction<
  typeof approveSchemaRequest
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
  transformGetSchemaRequests(mockedResponseSchemaRequests);

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
        'Search for an exact match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
    });

    it("shows a table with all schema requests and a header row", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const rows = within(table).getAllByRole("row");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(
        mockedApiResponseSchemaRequests.entries.length + 1
      );
    });
  });

  describe("renders pagination dependent on response", () => {
    beforeEach(() => {
      mockGetSchemaRequestsForApprover.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
    });

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

      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);

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

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows detail modal for first request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockedApiResponseSchemaRequests.entries[0];
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
        mockedApiResponseSchemaRequests.entries[
          mockedApiResponseSchemaRequests.entries.length - 1
        ];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View schema request for ${lastRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(lastRequest.topicname);
    });

    it("user can approve a request by clicking a button in the modal", async () => {
      mockApproveSchemaRequest.mockResolvedValue([{ result: "success" }]);
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockedApiResponseSchemaRequests.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View schema request for ${firstRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      const approveButton = within(modal).getByRole("button", {
        name: "Approve",
      });
      await userEvent.click(approveButton);

      await waitForElementToBeRemoved(modal);

      expect(mockApproveSchemaRequest).toHaveBeenCalledWith({
        reqIds: [firstRequest.req_no.toString()],
      });
    });

    it("user can decline a request by clicking a button in the modal", async () => {
      mockApproveSchemaRequest.mockResolvedValue([{ result: "success" }]);
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockedApiResponseSchemaRequests.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View schema request for ${firstRequest.topicname}`,
      });

      await userEvent.click(viewDetailsButton);

      const detailsModal = within(screen.getByRole("dialog")).queryByText(
        "Request details"
      );

      expect(detailsModal).toBeVisible();

      expect(
        within(screen.getByRole("dialog")).queryByRole("heading", {
          name: "Decline request",
        })
      ).not.toBeInTheDocument();

      const declineButton = screen.getByRole("button", {
        name: "Decline",
      });
      await userEvent.click(declineButton);

      expect(detailsModal).not.toBeInTheDocument();
      expect(
        within(screen.getByRole("dialog")).queryByRole("heading", {
          name: "Decline request",
        })
      ).toBeVisible();
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

      const environmentFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });
      const environmentOption = screen.getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      await userEvent.selectOptions(environmentFilter, environmentOption);

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

  describe("enables user to decline a request", () => {
    const testRequest = mockedApiResponseSchemaRequests.entries[0];

    beforeEach(async () => {
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

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("requires user to add a reason for declining", async () => {
      const declineButton = screen.getByRole("button", {
        name: `Decline schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);
      const modal = screen.getByRole("dialog");

      const confirmDeclineButton = within(modal).getByRole("button", {
        name: "Decline request",
      });
      expect(confirmDeclineButton).toBeDisabled();

      await userEvent.click(confirmDeclineButton);
      expect(mockDeclineSchemaRequest).not.toHaveBeenCalled();

      const message = within(modal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });
      expect(message).toBeRequired();

      await userEvent.type(message, "This is my message");
      await userEvent.tab();

      expect(confirmDeclineButton).toBeEnabled();
    });

    it("send a decline request api call if user declines a schema request", async () => {
      mockDeclineSchemaRequest.mockResolvedValue([{ result: "success" }]);

      const declineButton = screen.getByRole("button", {
        name: `Decline schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);
      const modal = screen.getByRole("dialog");

      const confirmDeclineButton = within(modal).getByRole("button", {
        name: "Decline request",
      });

      const message = within(modal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(message, "This is my message");
      await userEvent.tab();
      await userEvent.click(confirmDeclineButton);

      expect(mockDeclineSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
        reason: "This is my message",
      });
    });

    it("updates the the data for the table if user declined a schema request", async () => {
      mockDeclineSchemaRequest.mockResolvedValue([{ result: "success" }]);
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const declineButton = screen.getByRole("button", {
        name: `Decline schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);
      const modal = screen.getByRole("dialog");

      const confirmDeclineButton = within(modal).getByRole("button", {
        name: "Decline request",
      });

      const message = within(modal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(message, "This is my message");
      await userEvent.tab();
      await userEvent.click(confirmDeclineButton);

      expect(mockDeclineSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
        reason: "This is my message",
      });

      await waitForElementToBeRemoved(modal);
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
    });

    it("informs user about error if declining request was not successful", async () => {
      mockDeclineSchemaRequest.mockResolvedValue([{ result: "FAILURE" }]);
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const declineButton = screen.getByRole("button", {
        name: `Decline schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(declineButton);
      const modal = screen.getByRole("dialog");

      const confirmDeclineButton = within(modal).getByRole("button", {
        name: "Decline request",
      });

      const message = within(modal).getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(message, "This is my message");
      await userEvent.tab();
      await userEvent.click(confirmDeclineButton);

      expect(mockDeclineSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
        reason: "This is my message",
      });

      await waitForElementToBeRemoved(modal);
      expect(mockGetSchemaRequestsForApprover).not.toHaveBeenCalledTimes(2);

      const error = screen.getByRole("alert");
      expect(error).toBeVisible();
    });
  });

  describe("enables user to approve a request", () => {
    const testRequest = mockedApiResponseSchemaRequests.entries[0];

    beforeEach(async () => {
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

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("send a approve request api call if user approves a schema request", async () => {
      mockApproveSchemaRequest.mockResolvedValue([{ result: "success" }]);

      const approveButton = screen.getByRole("button", {
        name: `Approve schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
      });
    });

    it("updates the the data for the table if user approves a schema request", async () => {
      mockApproveSchemaRequest.mockResolvedValue([{ result: "success" }]);
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
      });

      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        2,
        defaultApiParams
      );
    });

    it("informs user about error if declining request was not successful", async () => {
      mockApproveSchemaRequest.mockResolvedValue([{ result: "FAILURE" }]);
      expect(mockGetSchemaRequestsForApprover).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const approveButton = screen.getByRole("button", {
        name: `Approve schema request for ${testRequest.topicname}`,
      });

      await userEvent.click(approveButton);

      expect(mockApproveSchemaRequest).toHaveBeenCalledWith({
        reqIds: [testRequest.req_no.toString()],
      });

      expect(mockApproveSchemaRequest).not.toHaveBeenCalledTimes(2);

      const error = await screen.findByRole("alert");
      expect(error).toBeVisible();
    });
  });
});
