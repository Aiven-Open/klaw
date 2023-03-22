import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  operationTypeList,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import OperationTypeFilter from "src/app/features/components/table-filters/OperationTypeFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const filterLabel = "Filter by operation type";

describe("OperationTypeFilter.tsx", () => {
  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<OperationTypeFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element for operation type", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for operation types", () => {
      const allOption = screen.getByRole("option", {
        name: "All operation types",
      });
      expect(allOption).toBeEnabled();

      operationTypeList.forEach((type) => {
        const option = screen.getByRole("option", {
          name: requestOperationTypeNameMap[type],
        });

        expect(option).toBeEnabled();
      });
    });
  });

  describe("sets the active operation type based on a query param", () => {
    const deleteOperation = "DELETE";
    const deleteName = requestOperationTypeNameMap[deleteOperation];

    beforeEach(async () => {
      const routePath = `/?operationType=${deleteOperation}`;

      customRender(<OperationTypeFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(`shows DELETE name as the active option one`, async () => {
      const select = await screen.findByRole("combobox", { name: filterLabel });
      const option = await within(select).findByRole("option", {
        name: deleteName,
        selected: true,
      });

      expect(select).toHaveValue(deleteOperation);
      expect(option).toBeVisible();
      expect(option).toHaveValue(deleteOperation);
    });
  });

  describe("handles user selecting a environment", () => {
    const createOperation = "CREATE";
    const approvedName = requestOperationTypeNameMap[createOperation];

    beforeEach(async () => {
      customRender(<OperationTypeFilter />, {
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

      expect(select).toHaveValue("ALL");

      const option = screen.getByRole("option", {
        name: approvedName,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(createOperation);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    const defaultOperationType = "DELETE";
    const deleteName = requestOperationTypeNameMap[defaultOperationType];

    beforeEach(async () => {
      customRender(<OperationTypeFilter />, {
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

    it(`sets "?operationType=${defaultOperationType}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: deleteName,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?operationType=${defaultOperationType}&page=1`
        );
      });
    });

    it(`unsets "?operationType" as search param when user selects All operation types`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const allOption = screen.getByRole("option", {
        name: "All operation types",
      });

      await userEvent.selectOptions(select, allOption);

      await waitFor(() => {
        expect(window.location.search).toEqual(`?page=1`);
      });
    });
  });
});
