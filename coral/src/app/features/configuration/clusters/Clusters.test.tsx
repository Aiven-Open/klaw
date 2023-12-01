import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Clusters } from "src/app/features/configuration/clusters/Clusters";
import { cleanup, screen, within } from "@testing-library/react";
import { ClusterDetails, getClustersPaginated } from "src/domain/cluster";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

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
    clusterType: "kafka",
    kafkaFlavor: "Aiven for Apache Kafka",
    projectName: "test-project-name",
    protocol: "SSL",
    publicKey: "",
    serviceName: "test-service-name",
    showDeleteCluster: true,
    totalNoPages: "1",
  },
  {
    allPageNos: ["1"],
    associatedServers: "https://otherproxy:11111",
    bootstrapServers: "11.111.11.111:9999",
    clusterId: 2,
    clusterName: "PROD",
    clusterStatus: "OFFLINE",
    clusterType: "kafkaconnect",
    kafkaFlavor: "Apache Kafka",
    protocol: "PLAINTEXT",
    publicKey: "",
    showDeleteCluster: true,
    totalNoPages: "1",
  },
];

describe("Clusters.tsx", () => {
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
});
