import { customRender } from "src/services/test-utils/render-with-wrappers";
import { ClusterTypeFilter } from "src/app/features/components/filters/ClusterTypeFilter";
import { cleanup, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import {
  clusterTypeMapList,
  clusterTypeToString,
} from "src/services/formatter/cluster-type-formatter";

const WrappedClusterTypeFilter = withFiltersContext({
  element: <ClusterTypeFilter />,
});

describe("ClusterTypeFilter", () => {
  const user = userEvent.setup();

  const selectLabel = "Filter by cluster type";
  const specialOptionNameForTypeAll = "All cluster types";

  describe("renders default view when no clusterType is set", () => {
    beforeAll(() => {
      customRender(<WrappedClusterTypeFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element", () => {
      const select = screen.getByRole("combobox", { name: selectLabel });

      expect(select).toBeEnabled();
    });

    it("shows the ALL value as selected by default", () => {
      const select = screen.getByRole("combobox", { name: selectLabel });

      expect(select).toHaveValue("ALL");
      expect(select).toHaveDisplayValue(specialOptionNameForTypeAll);
    });

    clusterTypeMapList.forEach((clusterTypeMapEntry) => {
      it(`shows an option for ${clusterTypeMapEntry.value}`, () => {
        const name =
          clusterTypeMapEntry.value === "ALL"
            ? specialOptionNameForTypeAll
            : clusterTypeMapEntry.name;
        const option = screen.getByRole("option", { name: name });

        expect(option).toBeVisible();
        expect(option).toHaveValue(clusterTypeMapEntry.value);
      });
    });
  });

  describe("selects the option based on a query param", () => {
    const selectedClusterType = "KAFKA";
    const routePath = `/cluster?clusterType=${selectedClusterType}`;

    beforeAll(async () => {
      customRender(<WrappedClusterTypeFilter />, {
        memoryRouter: true,
        customRoutePath: routePath,
      });
    });

    afterAll(cleanup);

    it("shows the KAFKA value as selected based on url query", () => {
      const select = screen.getByRole("combobox", { name: selectLabel });

      expect(select).toHaveValue(selectedClusterType);
    });
  });

  describe("sets the clusterType search parameter when user changes selected option", () => {
    const newOptionValue = "SCHEMA_REGISTRY";

    beforeEach(() => {
      customRender(<WrappedClusterTypeFilter />, {
        browserRouter: true,
      });
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      cleanup();
    });

    it("shows ALL as selected and no url query per default", () => {
      const select = screen.getByRole("combobox", { name: selectLabel });

      expect(select).toHaveValue("ALL");
      expect(window.location.search).toEqual("");
    });

    it("enables user to select different type", async () => {
      const select = screen.getByRole("combobox", { name: selectLabel });
      const option = screen.getByRole("option", {
        name: clusterTypeToString[newOptionValue],
      });

      await user.selectOptions(select, option);
      expect(select).toHaveValue(newOptionValue);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?clusterType=${newOptionValue}&page=1`
        );
      });
    });
  });
});
