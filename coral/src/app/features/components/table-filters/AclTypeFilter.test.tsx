import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import AclTypeFilter from "src/app/features/components/table-filters/AclTypeFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const aclTypesForFilter = ["ALL", "CONSUMER", "PRODUCER"];

const filterLabel = "Filter by ACL type";
describe("AclTypeFilter.tsx", () => {
  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<AclTypeFilter />, {
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
      aclTypesForFilter.forEach((acl) => {
        if (acl === "ALL") {
          const option = screen.getByRole("option", {
            name: "All ACL types",
          });
          expect(option).toBeEnabled();
          return;
        }

        const option = screen.getByRole("option", {
          name: acl,
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

  describe("sets the active environment based on a query param", () => {
    const consumerAcl = "CONSUMER";

    beforeEach(async () => {
      const routePath = `/?aclType=${consumerAcl}`;

      customRender(<AclTypeFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(`shows CONSUMER name as the active option one`, async () => {
      const option = await screen.findByRole("option", {
        name: consumerAcl,
        selected: true,
      });
      expect(option).toBeVisible();
      expect(option).toHaveValue(consumerAcl);
    });
  });

  describe("handles user selecting a environment", () => {
    const producerAcl = "PRODUCER";

    beforeEach(async () => {
      customRender(<AclTypeFilter />, {
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
        name: producerAcl,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(producerAcl);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    const consumerAcl = "CONSUMER";

    beforeEach(async () => {
      customRender(<AclTypeFilter />, {
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
        name: consumerAcl,
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
