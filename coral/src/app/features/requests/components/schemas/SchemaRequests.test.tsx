import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequests } from "src/app/features/requests/components/schemas/SchemaRequests";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { mockedApiResponseSchemaRequests } from "src/app/features/requests/components/schemas/utils/mocked-schema-requests";
import userEvent from "@testing-library/user-event";

jest.mock("src/domain/schema-request/schema-request-api.ts");

const mockGetSchemaRequests = getSchemaRequests as jest.MockedFunction<
  typeof getSchemaRequests
>;

describe("SchemaRequest", () => {
  const defaultApiParams = {
    pageNo: "1",
    topic: undefined,
  };

  beforeAll(() => {
    mockIntersectionObserver();
  });
  // This block still covers important cases, but it could be brittle
  // due to it's dependency on the async process of the api call
  // We'll add a helper for controlling api mocks better (get a loading state etc)
  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      // used to swallow a console.error that _should_ happen
      // while making sure to not swallow other console.errors
      console.error = jest.fn();

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

    it("shows a search input to search for topic names", () => {
      const search = screen.getByRole("search");

      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Search for an exact match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
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

  describe("handles filtering entries in the table", () => {
    beforeEach(async () => {
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

    it("enables user to search for topic", async () => {
      expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(
        1,
        defaultApiParams
      );

      const search = screen.getByRole("search");

      await userEvent.type(search, "myNiceTopic");

      await waitFor(() => {
        expect(mockGetSchemaRequests).toHaveBeenNthCalledWith(2, {
          ...defaultApiParams,
          topic: "myNiceTopic",
        });
      });
    });
  });
});
