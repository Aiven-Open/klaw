import { customRender } from "src/services/test-utils/render-with-wrappers";
import Clusters from "src/app/features/configuration/clusters/Clusters";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import {
  ClusterDetails,
  ClusterType,
  getClustersPaginated,
} from "src/domain/cluster";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { userEvent } from "@testing-library/user-event";
import { clusterTypeToString } from "src/services/formatter/cluster-type-formatter";
import {
  UseAuthContext,
  useAuthContext,
} from "src/app/context-provider/AuthProvider";

import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";

const INITIAL_AUTH_USER_CONTEXT_DATA: UseAuthContext = {
  ...testAuthUser,
  isSuperAdminUser: false,
};

jest.mock("src/domain/cluster/cluster-api.ts");
const mockGetClustersPaginated = getClustersPaginated as jest.MockedFunction<
  typeof getClustersPaginated
>;

jest.mock("src/app/context-provider/AuthProvider");
const mockUseAuthContext = useAuthContext as jest.MockedFunction<
  typeof useAuthContext
>;

const mockedUseToast = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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

    beforeAll(async () => {
      jest.spyOn(console, "error").mockImplementation((error) => error);
      mockGetClustersPaginated.mockRejectedValue(testError);
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
      expect(console.error).toHaveBeenCalledWith(testError);
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();
    });

    it("shows an error alert", async () => {
      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testError.message);
    });
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: testCluster,
      });
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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
        clusterType: "ALL",
      });
    });

    it("renders a select to filter by cluster type", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by cluster type",
      });

      expect(select).toBeEnabled();
    });

    it("renders a search field for cluster params", () => {
      const search = screen.getByRole("searchbox", {
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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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
        clusterType: "ALL",
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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when user searches for Cluster params", async () => {
      const testSearchInput = "MyCluster";

      const search = screen.getByRole("searchbox", {
        name: "Search Cluster parameters",
      });

      await user.type(search, testSearchInput);

      expect(search).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetClustersPaginated).toHaveBeenNthCalledWith(2, {
          pageNo: "1",
          clusterType: "ALL",
          searchClusterParam: testSearchInput,
        })
      );
    });
  });

  describe("handles user filtering for Cluster type", () => {
    beforeEach(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: testCluster,
      });
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

      customRender(<Clusters />, { queryClient: true, memoryRouter: true });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when user filters for a cluster type", async () => {
      const clusterType: ClusterType = "KAFKA";
      const clusterTypeString = clusterTypeToString[clusterType];

      const select = screen.getByRole("combobox", {
        name: "Filter by cluster type",
      });

      const option = screen.getByRole("option", { name: clusterTypeString });

      await user.selectOptions(select, option);

      expect(select).toHaveValue(clusterType);

      await waitFor(() =>
        expect(mockGetClustersPaginated).toHaveBeenNthCalledWith(2, {
          pageNo: "1",
          clusterType: clusterType,
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
      mockUseAuthContext.mockReturnValue(INITIAL_AUTH_USER_CONTEXT_DATA);

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

  describe("deletes a Cluster correctly when user has permission", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [testCluster[0]],
      });

      mockUseAuthContext.mockReturnValue({
        ...INITIAL_AUTH_USER_CONTEXT_DATA,
        permissions: {
          ...INITIAL_AUTH_USER_CONTEXT_DATA.permissions,
          addDeleteEditClusters: true,
        },
      });

      customRender(<Clusters />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders deletion menu and dispatch toast notification when user is allowed to delete cluster", async () => {
      const topicName = testCluster[0].clusterName;

      const table = screen.getByRole("table");
      const row = within(table).getByRole("row", {
        name: new RegExp(`${topicName}`),
      });
      const menuButton = within(row).getByRole("button", {
        name: "Context menu",
      });

      await userEvent.click(menuButton);

      const deleteItem = screen.getByRole("menuitem", {
        name: "Remove",
      });

      await userEvent.click(deleteItem);

      const modal = screen.getByRole("dialog");
      const deleteButton = within(modal).getByRole("button", {
        name: "Remove",
      });

      await userEvent.click(deleteButton);

      await waitFor(() => {
        expect(modal).not.toBeVisible();
        expect(mockedUseToast).toHaveBeenCalledWith({
          variant: "default",
          position: "bottom-left",
          message: `Cluster ${topicName} successfully removed`,
        });
      });
    });
  });

  describe("cannot delete Cluster if user does not have permission", () => {
    beforeAll(async () => {
      mockGetClustersPaginated.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [testCluster[0]],
      });

      mockUseAuthContext.mockReturnValue({
        ...INITIAL_AUTH_USER_CONTEXT_DATA,
        permissions: {
          ...INITIAL_AUTH_USER_CONTEXT_DATA.permissions,
          addDeleteEditClusters: false,
        },
      });

      customRender(<Clusters />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not render action menu in table", async () => {
      const topicName = testCluster[0].clusterName;

      const table = screen.getByRole("table");
      const row = within(table).getByRole("row", {
        name: new RegExp(`${topicName}`),
      });
      const menuButton = within(row).queryByRole("button", {
        name: "Context menu",
      });

      expect(menuButton).not.toBeInTheDocument();
    });
  });
});
