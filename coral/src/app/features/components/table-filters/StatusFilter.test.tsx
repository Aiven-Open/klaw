import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  requestStatusNameMap,
  statusList,
} from "src/app/features/approvals/utils/request-status-helper";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const filterLabel = "Filter by status";
describe("StatusFilter.tsx", () => {
  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<StatusFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element for status", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for statuse", () => {
      statusList.forEach((status) => {
        const option = screen.getByRole("option", {
          name: requestStatusNameMap[status],
        });

        expect(option).toBeEnabled();
      });
    });

    it("shows CREATED status name as the default active option", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName(requestStatusNameMap["CREATED"]);
    });
  });

  describe("sets the active environment based on a query param", () => {
    const declinedStatus = "DECLINED";
    const declinedName = requestStatusNameMap["DECLINED"];

    beforeEach(async () => {
      const routePath = `/?status=${declinedStatus}`;

      customRender(<StatusFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(`shows DECLINED name as the active option one`, async () => {
      const option = await screen.findByRole("option", {
        name: declinedName,
        selected: true,
      });
      expect(option).toBeVisible();
      expect(option).toHaveValue(declinedStatus);
    });
  });

  describe("handles user selecting a environment", () => {
    const approvedStatus = "APPROVED";
    const approvedName = requestStatusNameMap["APPROVED"];

    beforeEach(async () => {
      customRender(<StatusFilter />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it("sets the environment the user choose as active option", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: approvedName,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(approvedStatus);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    const deletedStatus = "DELETED";
    const deletedName = requestStatusNameMap["DELETED"];

    beforeEach(async () => {
      customRender(<StatusFilter />, {
        queryClient: true,
        browserRouter: true,
      });
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      cleanup();
    });

    it("shows no search param by default", async () => {
      expect(window.location.search).toEqual("");
    });

    it(`sets "?status=${deletedStatus}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: deletedName,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?status=${deletedStatus}&page=1`
        );
      });
    });
  });
});
