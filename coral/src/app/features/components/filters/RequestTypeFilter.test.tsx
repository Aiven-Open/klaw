import { cleanup, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  requestOperationTypeList,
  requestOperationTypeNameMap,
} from "src/app/features/approvals/utils/request-operation-type-helper";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const filterLabel = "Filter by request type";

describe("RequestTypeFilter.tsx", () => {
  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<RequestTypeFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element for request type", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for request types", () => {
      const allOption = screen.getByRole("option", {
        name: "All request types",
      });
      expect(allOption).toBeEnabled();

      requestOperationTypeList.forEach((type) => {
        const option = screen.getByRole("option", {
          name: requestOperationTypeNameMap[type],
        });

        expect(option).toBeEnabled();
      });
    });
  });

  describe("sets the active request type based on a query param", () => {
    const requestTypeDelete = "DELETE";
    const deleteName = requestOperationTypeNameMap[requestTypeDelete];

    beforeEach(async () => {
      const routePath = `/?requestType=${requestTypeDelete}`;

      customRender(<RequestTypeFilter />, {
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

      expect(select).toHaveValue(requestTypeDelete);
      expect(option).toBeVisible();
      expect(option).toHaveValue(requestTypeDelete);
    });
  });

  describe("handles user selecting an request type", () => {
    const requestTypeCreate = "CREATE";
    const approvedName = requestOperationTypeNameMap[requestTypeCreate];

    beforeEach(async () => {
      customRender(<RequestTypeFilter />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it("sets the request type the user choose as active option", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toHaveValue("ALL");

      const option = screen.getByRole("option", {
        name: approvedName,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(requestTypeCreate);
    });
  });

  describe("updates the search param to preserve request type in url", () => {
    const defaultRequestType = "DELETE";
    const deleteName = requestOperationTypeNameMap[defaultRequestType];

    beforeEach(async () => {
      customRender(<RequestTypeFilter />, {
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

    it(`sets "?requestType=${defaultRequestType}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: deleteName,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?requestType=${defaultRequestType}&page=1`
        );
      });
    });

    it(`unsets "?requestType" as search param when user selects All request types`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const allOption = screen.getByRole("option", {
        name: "All request types",
      });

      await userEvent.selectOptions(select, allOption);

      await waitFor(() => {
        expect(window.location.search).toEqual(`?page=1`);
      });
    });
  });
});
