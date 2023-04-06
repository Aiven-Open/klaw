import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { transformConnectorRequestApiResponse } from "src/domain/connector/connector-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { ConnectorRequests } from "src/app/features/requests/connectors/ConnectorRequests";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/app/features/requests/schemas/utils/mocked-api-responses";
import { getConnectorRequests } from "src/domain/connector";
import userEvent from "@testing-library/user-event";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/connector/connector-api.ts");

const mockGetConnectorEnvironmentRequest =
  getEnvironments as jest.MockedFunction<typeof getEnvironments>;

const mockGetConnectorRequests = getConnectorRequests as jest.MockedFunction<
  typeof getConnectorRequests
>;

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

describe("ConnectorRequests", () => {
  beforeEach(() => {
    mockIntersectionObserver();
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
    });
    expect(getConnectorRequests).toBeCalledTimes(1);
  });

  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      // used to swallow a console.error that _should_ happen
      // while making sure to not swallow other console.errors
      console.error = jest.fn();

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
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a loading state instead of a table while connector requests are being fetched", () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a error message in case of an error for fetching connector requests", async () => {
      mockGetConnectorRequests.mockRejectedValue("mock-error");

      customRender(<ConnectorRequests />, {
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
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequests).toHaveBeenCalledWith({
        pageNo: "100",
        search: "",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetConnectorRequests).toHaveBeenCalledWith({
        pageNo: "1",
        search: "",
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
      });
    });
  });

  describe("user can filter topic requests based on the topic name", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?search=",
      });
      expect(getConnectorRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        search: "",
      });
    });

    it("applies the topic filter by typing into to the search input", async () => {
      customRender(<ConnectorRequests />, {
        queryClient: true,
        memoryRouter: true,
      });
      const search = screen.getByRole("search");
      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Search for an partial match in name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
      await userEvent.type(search, "abc");
      await waitFor(() => {
        expect(getConnectorRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          search: "abc",
        });
      });
    });
  });
});
