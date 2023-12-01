import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Clusters } from "src/app/features/configuration/clusters/Clusters";
import { cleanup, screen } from "@testing-library/react";
import { getClustersPaginated } from "src/domain/cluster";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { KlawApiError } from "src/services/api";

jest.mock("src/domain/cluster/cluster-api.ts");
const mockGetClustersPaginated = getClustersPaginated as jest.MockedFunction<
  typeof getClustersPaginated
>;
describe("Clusters.tsx", () => {
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
});
