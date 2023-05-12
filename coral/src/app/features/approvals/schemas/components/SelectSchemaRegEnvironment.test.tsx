import { cleanup, screen } from "@testing-library/react";
import {
  ALL_ENVIRONMENTS_VALUE,
  getEnvironmentsForSchemaRequest,
} from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { SelectSchemaRegEnvironment } from "src/app/features/approvals/schemas/components/SelectSchemaRegEnvironment";

vi.mock("src/domain/environment/environment-api.ts");

const mockGetSchemaRegistryEnvironments = vi.mocked(
  getEnvironmentsForSchemaRequest
);

const filterLabel = "Filter by Environment";

describe("SelectSchemaRegEnvironment.tsx", () => {
  const mockedEnvironmentDev = createEnvironment({
    name: "DEV",
    id: "1",
  });
  const mockedEnvironmentTst = createEnvironment({
    name: "TST",
    id: "2",
  });

  describe("renders all necessary elements", () => {
    const mockedOnChange = vi.fn();

    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue([
        mockedEnvironmentDev,
        mockedEnvironmentTst,
      ]);

      customRender(
        <SelectSchemaRegEnvironment
          value={ALL_ENVIRONMENTS_VALUE}
          onChange={mockedOnChange}
        />,
        { queryClient: true }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterEach(cleanup);

    it("shows a select element for Environments", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for Environments plus a option for `All environments`", () => {
      mockedEnvironmentResponse.forEach((environment) => {
        const option = screen.getByRole("option", {
          name: environment.name,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(
        mockedEnvironmentResponse.length + 1
      );
    });

    it("shows `All Environments` as the active option based on given value", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All Environments");
    });

    it("has `All Environments` as the active value for option", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      expect(select).toHaveValue(ALL_ENVIRONMENTS_VALUE);
    });
  });

  describe("handles user selecting a environment", () => {
    const mockedOnChange = vi.fn();

    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue([
        mockedEnvironmentDev,
        mockedEnvironmentTst,
      ]);

      customRender(
        <SelectSchemaRegEnvironment
          value={ALL_ENVIRONMENTS_VALUE}
          onChange={mockedOnChange}
        />,
        { queryClient: true }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterEach(() => {
      vi.resetAllMocks();
      cleanup();
    });

    it("updates state for api call when user selects a new environment", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: mockedEnvironmentDev.name,
      });

      await userEvent.selectOptions(select, option);

      expect(mockedOnChange).toHaveBeenCalledWith(mockedEnvironmentDev.id);
    });
  });
});
