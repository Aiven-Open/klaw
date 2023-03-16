import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { cleanup, screen, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequests } from "src/app/features/requests/components/schemas/SchemaRequests";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { mockedApiResponseSchemaRequests } from "src/app/features/requests/components/schemas/utils/mocked-schema-requests";

jest.mock("src/domain/schema-request/schema-request-api.ts");

const mockGetSchemaRequests = getSchemaRequests as jest.MockedFunction<
  typeof getSchemaRequests
>;

describe("SchemaRequest", () => {
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
});
