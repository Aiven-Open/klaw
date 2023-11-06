import { cleanup, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";

import AclTypeFilter from "src/app/features/components/filters/AclTypeFilter";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import upperFirst from "lodash/upperFirst";

const aclTypesForFilter = ["ALL", "CONSUMER", "PRODUCER"];

const WrappedAclTypeFilter = withFiltersContext({ element: <AclTypeFilter /> });

const filterLabel = "Filter by ACL type";
describe("AclTypeFilter.tsx", () => {
  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<WrappedAclTypeFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element for ACL type", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for ACL type", () => {
      aclTypesForFilter.forEach((acl) => {
        const aclNameFormatted =
          acl === "ALL" ? "All ACL types" : upperFirst(acl.toLowerCase());

        const option = screen.getByRole("option", {
          name: aclNameFormatted,
        });
        expect(option).toBeEnabled();
      });
    });

    it("shows All Acl types as the default active option", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All ACL types");
    });
  });

  describe("sets the active ACL type based on a query param", () => {
    const consumerAcl = "CONSUMER";

    beforeEach(async () => {
      const routePath = `/?aclType=${consumerAcl}`;

      customRender(<WrappedAclTypeFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(`shows Consumer name as the active option one`, async () => {
      const aclNameFormatted = upperFirst(consumerAcl.toLowerCase());
      const option = await screen.findByRole("option", {
        name: aclNameFormatted,
        selected: true,
      });
      expect(option).toBeVisible();
      expect(option).toHaveValue(consumerAcl);
    });
  });

  describe("handles user selecting a ACL type", () => {
    const producerAcl = "PRODUCER";

    beforeEach(async () => {
      customRender(<WrappedAclTypeFilter />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it("sets the ACL type the user choose as active option", async () => {
      const aclNameFormatted = upperFirst(producerAcl.toLowerCase());
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: aclNameFormatted,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(producerAcl);
    });
  });

  describe("updates the search param to preserve ACL type in url", () => {
    const consumerAcl = "CONSUMER";
    const consumerAclFormatted = upperFirst(consumerAcl.toLowerCase());

    beforeEach(async () => {
      customRender(<WrappedAclTypeFilter />, {
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

    it(`sets "?aclType=${consumerAcl}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: consumerAclFormatted,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?aclType=${consumerAcl}&page=1`
        );
      });
    });
  });
});
