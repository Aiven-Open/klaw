import { customRender } from "src/services/test-utils/render-with-wrappers";
import Clusters from "src/app/features/configuration/clusters/Clusters";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { ClusterDetails, getClustersPaginated } from "src/domain/cluster";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { userEvent } from "@testing-library/user-event";

jest.mock("src/domain/cluster/cluster-api.ts");
const mockGetClustersPaginated = getClustersPaginated as jest.MockedFunction<
  typeof getClustersPaginated
>;

const testCluster: ClusterDetails[] = [
  {
    allPageNos: ["1"],
    associatedServers: "https://testrestproxy:11111",
    bootstrapServers: "this-is-a-test.aivencloud.com:11111",
    clusterId: 1,
    clusterName: "DEV",
    clusterStatus: "OFFLINE",
    clusterType: "KAFKA",
    kafkaFlavor: "AIVEN_FOR_APACHE_KAFKA",
    projectName: "test-project-name",
    protocol: "SSL",
    serviceName: "test-service-name",
    showDeleteCluster: true,
    totalNoPages: "1",
    currentPage: "1",
  },
  {
    allPageNos: ["1"],
    associatedServers: "https://otherproxy:11111",
    bootstrapServers: "11.111.11.111:9999",
    clusterId: 2,
    clusterName: "PROD",
    clusterStatus: "OFFLINE",
    clusterType: "KAFKA_CONNECT",
    kafkaFlavor: "APACHE_KAFKA",
    protocol: "PLAINTEXT",
    showDeleteCluster: true,
    totalNoPages: "1",
    currentPage: "1",
  },
];

describe("Clusters.tsx", () => {
  const user = userEvent.setup();

  beforeAll(mockIntersectionObserver);

  describe("handles loading state", () => {
    beforeAll(() => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a loading information", () => {
      const loadingAnimation = screen.getByTestId("skeleton-table");
      expect(loadingAnimation).toBeVisible();
    });
  });

  describe("handles empty state", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();
    });

    it("shows information about the empty state", () => {
      const heading = screen.getByRole("heading", { name: "No Clusters" });
      expect(heading).toBeVisible();
    });
  });

  describe("handles error state", () => {
    const testError: KlawApiError = {
      message: "OH NO 😭",
      success: false,
    };

    const originalConsoleError = console.error;

    beforeAll(async () => {
      console.error = jest.fn();
      mockGetClustersPaginated.mockRejectedValue(testError);
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
      console.error = originalConsoleError;
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalled();
    });

    it("shows an error alert", () => {
      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testError.message);
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: testCluster,
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches data with default params on page load", async () => {
      expect(mockGetClustersPaginated).toHaveBeenCalledWith({
        pageNo: "1",
      });
    });

    it("renders a search field for cluster params", () => {
      const search = screen.getByRole("search", {
        name: "Search Cluster parameters",
      });

      expect(search).toBeEnabled();
    });

    it("renders the cluster table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Cluster overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows cluster names row headers", () => {
      const table = screen.getByRole("table", {
        name: "Cluster overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: testCluster[0].clusterName,
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

  describe("handles successful response with three page", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 2,
        totalPages: 3,
        entries: testCluster,
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders the cluster table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Cluster overview, page 2 of 3",
      });

      expect(table).toBeVisible();
    });

    it("does render the pagination with information about the pages", () => {
      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 3",
      });

      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: testCluster,
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 3 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 3 of 5"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 4",
      });

      await user.click(pageTwoButton);

      expect(mockGetClustersPaginated).toHaveBeenNthCalledWith(2, {
        pageNo: "4",
      });
    });
  });

  describe("handles user searching for Cluster params", () => {
    beforeEach(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: testCluster,
      });
      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when user searches for Cluster params", async () => {
      const testSearchInput = "MyCluster";

      const search = screen.getByRole("search", {
        name: "Search Cluster parameters",
      });

      await user.type(search, testSearchInput);

      expect(search).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetClustersPaginated).toHaveBeenNthCalledWith(2, {
          pageNo: "1",
          searchClusterParam: testSearchInput,
        })
      );
    });
  });

  describe("renders ClusterConnectHelpModal on first load when needed", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [testCluster[0]],
      });

      customRender(<Clusters />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: `/configuration/clusters?search=${testCluster[0].clusterName}&showConnectHelp=true`,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders ClusterConnectHelpModal on first render if correct query params are set", () => {
      const modal = screen.getByRole("dialog");
      expect(modal).toBeVisible();
      expect(within(modal).getByText("Connect cluster to Klaw")).toBeVisible();
    });

    it("remove showConnectHelp query param when closing ClusterConnectHelpModal ", async () => {
      const modal = screen.getByRole("dialog");
      expect(modal).toBeVisible();

      const closeButton = within(modal).getByRole("button", { name: "Done" });

      await userEvent.click(closeButton);

      expect(window.location.search).not.toContain("showConnectHelp=true");
    });
  });
});
