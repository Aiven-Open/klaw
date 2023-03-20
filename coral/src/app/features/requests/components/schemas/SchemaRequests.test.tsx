import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import {
  cleanup,
  screen,
  waitFor,
  within,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequests } from "src/app/features/requests/components/schemas/SchemaRequests";
import {
  mockedApiResponseSchemaRequests,
  mockedEnvironmentResponse,
} from "src/app/features/requests/components/schemas/utils/mocked-api-responses";
import userEvent from "@testing-library/user-event";
import { getSchemaRegistryEnvironments } from "src/domain/environment";
import { requestStatusNameMap } from "src/app/features/approvals/utils/request-status-helper";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/schema-request/schema-request-api.ts");

const mockGetSchemaRegistryEnvironments =
  getSchemaRegistryEnvironments as jest.MockedFunction<
    typeof getSchemaRegistryEnvironments
  >;

const mockGetSchemaRequests = getSchemaRequests as jest.MockedFunction<
  typeof getSchemaRequests
>;

describe("SchemaRequest", () => {
  const defaultApiParams = {
    env: "ALL",
    pageNo: "1",
    requestStatus: "ALL",
    topic: undefined,
  };

  beforeAll(() => {
    mockIntersectionObserver();
  });
  // This block still covers important cases, but it could be brittle
  // due to its dependency on the async process of the api call
  // We'll add a helper for controlling api mocks better (get a loading state etc)
  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      // used to swallow a console.error that _should_ happen
      // while making sure to not swallow other console.errors
      console.error = jest.fn();

      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue({
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

    it("shows a loading state instead of a table while schema requests are being fetched", () => {
      customRender(<SchemaRequests />, {
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
      mockGetSchemaRequests.mockRejectedValue("mock-error");

      customRender(<SchemaRequests />, {
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
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
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

    it("shows a select to filter by request status with default", () => {
      const select = screen.getByLabelText("Filter by status");

      expect(select).toBeVisible();
      expect(select).toHaveDisplayValue("All statuses");
    });

    it("shows a search input to search for topic names", () => {
      const search = screen.getByRole("search");

      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Search for an exact match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
    });

    it("shows a toggle to only show users own requests", () => {
      const toggle = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });

      expect(toggle).toBeVisible();
    });

    it("shows a table with all schema requests", () => {
      const table = screen.getByRole("table", { name: "Schema requests" });
      const rows = within(table).getAllByRole("row");
      const headerRow = 1;

      expect(table).toBeVisible();
      expect(rows).toHaveLength(
        mockedApiResponseSchemaRequests.entries.length + headerRow
      );
    });
  });

  describe("renders pagination dependent on response", () => {
    beforeEach(() => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = `/?page=100`;
      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetSchemaRequests).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetSchemaRequests).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetSchemaRequests.mockResolvedValue({
        ...mockedApiResponseSchemaRequests,
        totalPages: 1,
      });

      customRender(<SchemaRequests />, {
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
      mockGetSchemaRequests.mockResolvedValue({
        totalPages: 4,
        currentPage: 1,
        entries: [],
      });

      customRender(<SchemaRequests />, {
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
      mockGetSchemaRequests.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<SchemaRequests />, {
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
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<SchemaRequests />, {
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

      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        pageNo: "2",
      });
    });
  });

  describe("user can filter schema requests by 'environment'", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
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
      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(1, {
        ...defaultApiParams,
        env: "TEST_ENV_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
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

      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        env: mockedEnvironmentResponse[0].id,
      });
    });
  });

  describe("user can filter schema requests by 'status'", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
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
      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(1, {
        ...defaultApiParams,
        requestStatus: "TEST_STATUS_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
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

      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        requestStatus: newStatus,
      });
    });
  });

  describe("user can filter schema requests by 'topic' they searched for", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?topic=TEST_SEARCH_VALUE",
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(1, {
        ...defaultApiParams,
        topic: "TEST_SEARCH_VALUE",
      });
    });

    it("enables user to search for topic", async () => {
      const search = screen.getByRole("search");

      // since the search term is persisted, it's the current
      // value of the search element.
      await userEvent.clear(search);
      await userEvent.type(search, "myNiceTopic");

      await waitFor(() => {
        expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
          ...defaultApiParams,
          topic: "myNiceTopic",
        });
      });
    });
  });

  describe("user can filter schema requests by only showing their own requests", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?showOnlyMyRequests=true",
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(1, {
        ...defaultApiParams,
        isMyRequest: true,
      });
    });

    it("enables user to toggle only showing their own requests", async () => {
      const toggle = screen.getByRole("checkbox", {
        name: "Show only my requests",
      });

      expect(toggle).toBeChecked();
      await userEvent.click(toggle);

      await waitFor(() => {
        expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
          ...defaultApiParams,
          isMyRequest: undefined,
        });
      });
    });
  });

  describe("shows a detail modal for schema request", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedEnvironmentResponse
      );
      mockGetSchemaRequests.mockResolvedValue(mockedApiResponseSchemaRequests);

      customRender(<SchemaRequests />, {
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
  });
});
