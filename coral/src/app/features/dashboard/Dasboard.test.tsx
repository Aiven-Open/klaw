import { cleanup, screen } from "@testing-library/react";
import Dashboard from "src/app/features/dashboard/Dashboard";
import { mockResizeObserver } from "src/services/test-utils/mock-resize-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("Dashboard.tsx", () => {
  describe("renders correct sections", () => {
    beforeEach(async () => {
      mockResizeObserver();

      customRender(<Dashboard />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it("renders Topics section", async () => {
      const section = screen.getByText("Topics");

      expect(section).toBeVisible();
    });

    it("renders Pending requests section", async () => {
      const section = screen.getByText("Pending requests");

      expect(section).toBeVisible();
    });

    it("renders Requests per day section", async () => {
      const section = screen.getByText("Requests per day");

      expect(section).toBeVisible();
    });

    it("renders Analytics section", async () => {
      const section = screen.getByText("Analytics");

      expect(section).toBeVisible();
    });
  });
});
