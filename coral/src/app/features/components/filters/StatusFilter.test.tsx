import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  requestStatusNameMap,
  statusList,
} from "src/app/features/approvals/utils/request-status-helper";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const filterLabel = "Filter by status";

describe("StatusFilter.tsx", () => {
  const testDefaultStatus = "CREATED";
  const WrappedStatusFilter = withFiltersContext({
    defaultValues: { status: testDefaultStatus },
    element: <StatusFilter />,
  });
  describe("renders all necessary elements", () => {
    beforeEach(() => {
      customRender(<WrappedStatusFilter />, {
        memoryRouter: true,
      });
    });

    afterEach(cleanup);

    it("shows a select element for status", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for statuses", () => {
      statusList.forEach((status) => {
        const option = screen.getByRole("option", {
          name: requestStatusNameMap[status],
        });

        expect(option).toBeEnabled();
      });
    });

    it("shows a status name as the default active option based on prop", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = within(select).getByRole("option", {
        selected: true,
      });

      expect(select).toHaveValue(testDefaultStatus);
      expect(option).toHaveAccessibleName(
        requestStatusNameMap[testDefaultStatus]
      );
    });
  });

  describe("sets the active status based on a query param", () => {
    const declinedStatus = "DECLINED";
    const declinedName = requestStatusNameMap[declinedStatus];

    beforeEach(async () => {
      const routePath = `/?status=${declinedStatus}`;

      customRender(<WrappedStatusFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(`shows DECLINED name as the active option one`, async () => {
      const select = await screen.findByRole("combobox", { name: filterLabel });
      const option = await within(select).findByRole("option", {
        name: declinedName,
        selected: true,
      });

      expect(select).toHaveValue(declinedStatus);
      expect(option).toBeVisible();
      expect(option).toHaveValue(declinedStatus);
    });
  });

  describe("handles user selecting a status", () => {
    const defaultStatus = "DECLINED";
    const approvedStatus = "APPROVED";
    const approvedName = requestStatusNameMap[approvedStatus];
    const WrappedStatusFilter = withFiltersContext({
      defaultValues: { status: defaultStatus },
      element: <StatusFilter />,
    });

    beforeEach(async () => {
      customRender(<WrappedStatusFilter />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it("sets the status the user choose as active option", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toHaveValue(defaultStatus);

      const option = screen.getByRole("option", {
        name: approvedName,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(approvedStatus);
    });
  });

  describe("updates the search param to preserve status in url", () => {
    const deletedStatus = "DELETED";
    const deletedName = requestStatusNameMap["DELETED"];
    const WrappedStatusFilter = withFiltersContext({
      defaultValues: { status: "CREATED" },
      element: <StatusFilter />,
    });

    beforeEach(async () => {
      customRender(<WrappedStatusFilter />, {
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
