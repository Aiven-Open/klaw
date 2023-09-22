import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import KafkaConnectEnvironments from "src/app/features/configuration/environments/KafkaConnect/KafkaConnectEnvironments";
import { getPaginatedEnvironmentsForConnector } from "src/domain/environment";
import { transformPaginatedEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { EnvironmentPaginatedApiResponse } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

const defaultApiParams = {
  pageNo: "1",
  searchEnvParam: undefined,
};

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/environment/environment-api.ts");

const mockGetPaginatedEnvironmentsForConnector =
  getPaginatedEnvironmentsForConnector as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForConnector
  >;

const mockedEnvironmentsResponse: EnvironmentPaginatedApiResponse =
  transformPaginatedEnvironmentApiResponse([
    {
      id: "11",
      name: "PRD",
      type: "kafkaconnect",
      tenantId: 101,
      clusterId: 8,
      tenantName: "default",
      clusterName: "TST_CONNCT",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      otherParams: "",
      params: {},
    },
    {
      id: "6",
      name: "ACC",
      type: "kafkaconnect",
      tenantId: 101,
      clusterId: 3,
      tenantName: "default",
      clusterName: "DEV",
      envStatus: "OFFLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      otherParams: "",
      params: {},
    },
    {
      id: "4",
      name: "DEV",
      type: "kafkaconnect",
      tenantId: 101,
      clusterId: 6,
      tenantName: "default",
      clusterName: "UIKLAW",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      otherParams: "",
      params: {},
    },
    {
      id: "10",
      name: "TST",
      type: "kafkaconnect",
      tenantId: 101,
      clusterId: 6,
      tenantName: "default",
      clusterName: "UIKLAW",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      otherParams: "",
      params: {},
    },
  ]);

describe("KafkaConnectEnvironments.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<KafkaConnectEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Kafka Connect Environments table with information about the pages", () => {
      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows Environment names row headers", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockedEnvironmentsResponse.entries[0].name,
      });
      expect(rowHeader).toBeVisible();
    });

    it("render the correct amount of rows", async () => {
      const rows = screen.getAllByRole("row");

      expect(rows).toHaveLength(5);
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
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 3,
        currentPage: 2,
      });

      customRender(<KafkaConnectEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Kafka Connect Environments table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Connect Environments overview, page 2 of 3",
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
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 4,
        currentPage: 2,
      });

      customRender(<KafkaConnectEnvironments />, {
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

      expect(mockGetPaginatedEnvironmentsForConnector).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "3",
      });
    });
  });

  describe("handles user searching with search input", () => {
    const testSearchInput = "My search name";

    beforeEach(async () => {
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<KafkaConnectEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when when user enters text in input", async () => {
      const search = screen.getByRole("search", {
        name: "Search Kafka Connect Environment",
      });
      expect(search).toHaveValue("");

      await userEvent.type(search, testSearchInput);

      expect(search).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetPaginatedEnvironmentsForConnector).toHaveBeenCalledWith({
          ...defaultApiParams,
          searchEnvParam: testSearchInput,
        })
      );
    });

    it("enables user to navigate to search input with keyboard", async () => {
      const search = screen.getByRole("search", {
        name: "Search Kafka Connect Environment",
      });

      expect(search).toHaveValue("");

      await tabNavigateTo({ targetElement: search });

      expect(search).toHaveFocus();
    });
  });
});
