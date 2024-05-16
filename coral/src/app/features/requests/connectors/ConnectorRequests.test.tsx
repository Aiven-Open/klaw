import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { requestOperationTypeNameMap } from "src/app/features/approvals/utils/request-operation-type-helper";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";
import ConnectorRequests from "src/app/features/requests/connectors/ConnectorRequests";
import { mockedEnvironmentResponse } from "src/app/features/requests/schemas/utils/mocked-api-responses";
import {
  deleteConnectorRequest,
  getConnectorRequests,
} from "src/domain/connector";
import { transformConnectorRequestApiResponse } from "src/domain/connector/connector-transformer";
import {
  getAllEnvironmentsForConnector,
  getAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/connector/connector-api.ts");

const mockGetConnectorEnvironmentRequest =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetConnectorRequests = getConnectorRequests as jest.MockedFunction<
  typeof getConnectorRequests
>;

const mockGetSyncConnectorsEnvironments =
  getAllEnvironmentsForConnector as jest.MockedFunction<
    typeof getAllEnvironmentsForConnector
  >;

const mockDeleteConnectorRequest =
  deleteConnectorRequest as jest.MockedFunction<typeof deleteConnectorRequest>;

const mockGetConnectorRequestsResponse = transformConnectorRequestApiResponse([
  {
    connectorName: "test-connector-1",
    environment: "1",
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This connector is for test",
    environmentName: "BRG",
    connectorId: 1000,
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
    editable: true,
    deletable: true,
    connectorConfig: "",
  },
]);

const mockEnvironments = [
  createMockEnvironmentDTO({
    id: "1",
    name: "DEV",
  }),
  createMockEnvironmentDTO({
    id: "2",
    name: "TST",
  }),
];

describe("ConnectorRequests", () => {
  beforeEach(() => {
    mockIntersectionObserver();
    mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
    mockGetConnectorEnvironmentRequest.mockResolvedValue(
      mockedEnvironmentResponse
    );
    mockGetConnectorRequests.mockResolvedValue(
      mockGetConnectorRequestsResponse
    );
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("makes a request to the api to get the teams connector requests", () => {
    customRender(<ConnectorRequests />, {
      queryClient: true,
      memoryRouter: true,
      aquariumContext: true,
    });
    expect(getConnectorRequests).toBeCalledTimes(1);
  });

  describe("handles loading and error state when fetching the requests", () => {
    beforeEach(() => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a loading state instead of a table while connector requests are being fetched", () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
    });

    it("shows a error message in case of an error for fetching connector requests", async () => {
      mockGetConnectorRequests.mockRejectedValue("mock-error");

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
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

  describe("user can browse the requests in paged sets", () => {
    beforeEach(() => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue({
        totalPages: 1,
        currentPage: 1,
        entries: [],
      });
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = "/?page=100";
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequests).toHaveBeenCalledWith({
        pageNo: "100",
        search: "",
        env: "ALL",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequests).toHaveBeenCalledWith({
        pageNo: "1",
        search: "",
        env: "ALL",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetConnectorRequests.mockResolvedValue({
        ...mockGetConnectorRequestsResponse,
        totalPages: 1,
      });

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });
      expect(pagination).not.toBeInTheDocument();
    });

    it("shows a pagination when response has more then one page", async () => {
      mockGetConnectorRequests.mockResolvedValue({
        totalPages: 2,
        currentPage: 1,
        entries: [],
      });

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 2",
      });
      expect(pagination).toBeVisible();
    });

    it("shows the currently active page based on api response", async () => {
      mockGetConnectorRequests.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
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
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
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

      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "2",
        search: "",
        env: "ALL",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });
  });

  describe("user can filter connector requests based on the connector name", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath: "/?search=",
      });
      expect(getConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "ALL",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });

    it("applies the search filter by typing into to the search input", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      const search = screen.getByRole("searchbox", {
        name: "Search Connector",
      });
      expect(search).toBeVisible();

      await userEvent.type(search, "abc");
      await waitFor(() => {
        expect(getConnectorRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "abc",
          env: "ALL",
          isMyRequest: false,
          requestStatus: "ALL",
          operationType: "ALL",
        });
      });
    });
  });

  describe("user can filter connector requests by 'environment'", () => {
    beforeEach(async () => {
      mockGetSyncConnectorsEnvironments.mockResolvedValue(mockEnvironments);
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue(
        mockGetConnectorRequestsResponse
      );
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath:
          "/?environment=TEST_ENV_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        env: "TEST_ENV_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });

    it("enables user to filter by 'environment'", async () => {
      const environmentFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      const environmentOption = screen.getByRole("option", {
        name: mockedEnvironmentResponse[0].name,
      });
      await userEvent.selectOptions(environmentFilter, environmentOption);

      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        search: "",
        env: mockedEnvironmentResponse[0].id,
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });
  });

  describe("user can filter connector requests to only display users own requests", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the MyRequests filter from the url search parameters", () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath: "/?showOnlyMyRequests=true",
      });
      expect(getConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        isMyRequest: true,
        search: "",
        env: "ALL",
        requestStatus: "ALL",
        operationType: "ALL",
      });
    });

    it("applies the MyRequest filter by toggling the switch", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });
      const isMyRequestSwitch = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });
      await userEvent.click(isMyRequestSwitch);
      await waitFor(() => {
        expect(getConnectorRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          isMyRequest: true,
          search: "",
          env: "ALL",
          requestStatus: "ALL",
          operationType: "ALL",
        });
      });
    });

    it("un-applies the MyRequest filter by un-toggling the switch", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath: "/?showOnlyMyRequests=true",
      });
      const isMyRequestSwitch = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });
      await userEvent.click(isMyRequestSwitch);
      await waitFor(() => {
        expect(getConnectorRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          isMyRequest: false,
          search: "",
          env: "ALL",
          requestStatus: "ALL",
          operationType: "ALL",
        });
      });
    });
  });

  describe("user can filter connector requests by 'status'", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue(
        mockGetConnectorRequestsResponse
      );

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath:
          "/?status=TEST_STATUS_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: "TEST_STATUS_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
        env: "ALL",
        operationType: "ALL",
      });
    });

    it("enables user to filter by 'status'", async () => {
      const newStatus = "CREATED";

      const statusFilter = screen.getByRole("combobox", {
        name: "Filter by status",
      });
      const statusOption = screen.getByRole("option", {
        name: requestStatusNameMap[newStatus],
      });
      await userEvent.selectOptions(statusFilter, statusOption);

      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: newStatus,
        env: "ALL",
        operationType: "ALL",
      });
    });
  });

  describe("user can filter connector requests by operation type", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue(
        mockGetConnectorRequestsResponse
      );

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
        customRoutePath: "/?requestType=DELETE",
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        isMyRequest: false,
        operationType: "DELETE",
        requestStatus: "ALL",
        env: "ALL",
        search: "",
      });
    });

    it("enables user to filter by 'status'", async () => {
      const newType = "PROMOTE";

      const statusFilter = screen.getByRole("combobox", {
        name: "Filter by request type",
      });
      const statusOption = screen.getByRole("option", {
        name: requestOperationTypeNameMap[newType],
      });
      await userEvent.selectOptions(statusFilter, statusOption);

      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        isMyRequest: false,
        requestStatus: "ALL",
        operationType: newType,
        env: "ALL",
        search: "",
      });
    });
  });

  describe("enables user to delete a request", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue(
        mockGetConnectorRequestsResponse
      );

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("send a delete request api call if user deletes a Connector request", async () => {
      mockDeleteConnectorRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);

      const deleteButton = screen.getByRole("button", {
        name: "Delete connector request for test-connector-1",
      });

      await userEvent.click(deleteButton);
      const dialog = screen.getByRole("dialog");

      const confirmDeclineButton = within(dialog).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(confirmDeclineButton);

      expect(mockDeleteConnectorRequest).toHaveBeenCalledWith({
        reqIds: ["1000"],
      });
    });

    it("updates the the data for the table if user deletes a Connector request", async () => {
      mockDeleteConnectorRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: "ALL",
        env: "ALL",
        operationType: "ALL",
      });

      const deleteButton = screen.getByRole("button", {
        name: "Delete connector request for test-connector-1",
      });

      await userEvent.click(deleteButton);
      const modal = screen.getByRole("dialog");

      const confirmDelete = within(modal).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(confirmDelete);

      expect(mockDeleteConnectorRequest).toHaveBeenCalledWith({
        reqIds: ["1000"],
      });

      expect(modal).not.toBeVisible();
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: "ALL",
        env: "ALL",
        operationType: "ALL",
      });
    });

    it("informs user about error if deleting request was not successful", async () => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);

      mockDeleteConnectorRequest.mockRejectedValue({ message: "OH NO" });
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: "ALL",
        env: "ALL",
        operationType: "ALL",
      });

      const deleteButton = screen.getByRole("button", {
        name: `Delete connector request for test-connector-1`,
      });

      await userEvent.click(deleteButton);
      const modal = screen.getByRole("dialog");

      const confirmDeleteButton = within(modal).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(confirmDeleteButton);

      expect(mockDeleteConnectorRequest).toHaveBeenCalledWith({
        reqIds: ["1000"],
      });

      expect(modal).not.toBeVisible();
      expect(mockGetConnectorRequests).not.toHaveBeenCalledTimes(2);

      const error = screen.getByRole("alert");
      expect(error).toBeVisible();
      expect(console.error).toHaveBeenCalledWith({ message: "OH NO" });
    });

    it("informs user about error if deleting request was not successful and error is hidden in success", async () => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);

      mockDeleteConnectorRequest.mockRejectedValue("OH NO");
      expect(mockGetConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
        isMyRequest: false,
        requestStatus: "ALL",
        env: "ALL",
        operationType: "ALL",
      });

      const deleteButton = screen.getByRole("button", {
        name: `Delete connector request for test-connector-1`,
      });

      await userEvent.click(deleteButton);
      const modal = screen.getByRole("dialog");

      const confirmDeleteButton = within(modal).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(confirmDeleteButton);

      expect(mockDeleteConnectorRequest).toHaveBeenCalledWith({
        reqIds: ["1000"],
      });

      expect(modal).not.toBeVisible();
      expect(mockGetConnectorRequests).not.toHaveBeenCalledTimes(2);

      const error = screen.getByRole("alert");
      expect(error).toBeVisible();

      expect(console.error).toHaveBeenCalledWith("OH NO");
    });
  });

  describe("shows a detail modal for connector request", () => {
    beforeEach(async () => {
      mockGetConnectorEnvironmentRequest.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetConnectorRequests.mockResolvedValue(
        mockGetConnectorRequestsResponse
      );

      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows detail modal for first request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const firstRequest = mockGetConnectorRequestsResponse.entries[0];
      const viewDetailsButton = screen.getByRole("button", {
        name: `View connector request for ${firstRequest.connectorName}`,
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent(firstRequest.connectorName);
    });

    it("shows detail modal for last request returned from the api", async () => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const viewDetailsButton = screen.getByRole("button", {
        name: "View connector request for test-connector-1",
      });

      await userEvent.click(viewDetailsButton);
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
      expect(modal).toHaveTextContent("test-connector-1");
    });

    it("user can delete a request by clicking a button in the modal", async () => {
      mockDeleteConnectorRequest.mockResolvedValue([
        { success: true, message: "" },
      ]);
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

      const viewDetailsButton = screen.getByRole("button", {
        name: `View connector request for test-connector-1`,
      });

      await userEvent.click(viewDetailsButton);

      const detailsModal = within(screen.getByRole("dialog")).queryByText(
        "Request details"
      );

      expect(detailsModal).toBeVisible();

      expect(
        within(screen.getByRole("dialog")).queryByRole("heading", {
          name: "Delete request",
        })
      ).not.toBeInTheDocument();

      const deleteButton = screen.getByRole("button", {
        name: "Delete",
      });
      await userEvent.click(deleteButton);

      expect(detailsModal).not.toBeInTheDocument();
      expect(
        within(screen.getByRole("dialog")).queryByRole("heading", {
          name: "Delete request",
        })
      ).toBeVisible();
    });
  });
});
