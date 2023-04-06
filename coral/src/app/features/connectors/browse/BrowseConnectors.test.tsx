import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getConnectors } from "src/domain/connector";

jest.mock("src/domain/connector/connector-api.ts");
const mockGetConnectors = getConnectors as jest.MockedFunction<
  typeof getConnectors
>;

const mockConnectors = [
  {
    sequence: 2,
    connectorId: 1001,
    connectorName: "test_connector_1",
    environmentId: "4",
    teamName: "Dev",
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["DEV"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
  {
    sequence: 2,
    connectorId: 1002,
    connectorName: "test_connector_2",
    environmentId: "4",
    teamName: "Ospo",
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["DEV", "TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
  {
    sequence: 2,
    connectorId: 1003,
    connectorName: "test_connector_3",
    environmentId: "4",
    teamName: "Infra",
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
];

const mockResponseDefault = {
  totalPages: 1,
  currentPage: 1,
  entries: mockConnectors,
};

describe("BrowseConnectors.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetConnectors.mockResolvedValue(mockResponseDefault);

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Connector table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows Connector names row headers", () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockResponseDefault.entries[0].connectorName,
      });
      expect(rowHeader).toBeVisible();
    });

    it("does not render the pagination", () => {
      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).not.toBeInTheDocument();
    });
  });

  describe("handles successful response with three pages", () => {
    beforeAll(async () => {
      mockGetConnectors.mockResolvedValue({
        ...mockResponseDefault,
        totalPages: 3,
        currentPage: 2,
      });

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Connector table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 2 of 3",
      });

      expect(table).toBeVisible();
    });

    it("renders the pagination with information about the pages", () => {
      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 3",
      });

      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetConnectors.mockResolvedValue({
        ...mockResponseDefault,
        totalPages: 4,
        currentPage: 2,
      });

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 2 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 2 of 4"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 3",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetConnectors).toHaveBeenNthCalledWith(2, {
        currentPage: 3,
        environment: "ALL",
      });
    });
  });
});
