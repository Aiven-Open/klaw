import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Environment } from "src/domain/environment";
import SelectEnvironment from "src/app/features/browse-topics/components/select-environment/SelectEnvironment";

describe("SelectEnvironment.tsx", () => {
  const environments = ["ALL", "DEV", "TST"].map((env) => ({
    label: env,
    value: env,
  }));

  describe("renders all necessary elements", () => {
    const activeOption: Environment = "ALL";

    const requiredProps = {
      environments,
      activeOption,
      selectEnvironment: jest.fn(),
    };

    beforeAll(() => {
      render(<SelectEnvironment {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a select element for Kafka environments", () => {
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of given options for environments", () => {
      environments.forEach(({ label }) => {
        const option = screen.getByRole("option", {
          name: label,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(environments.length);
    });

    it("shows a given environment as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName(activeOption);
    });
  });

  describe("handles the change event for selecting", () => {
    const optionToSelect = "DEV";

    const mockedSelectEnvironment = jest.fn();
    const requiredProps = {
      environments,
      activeOption: "ALL",
      selectEnvironment: mockedSelectEnvironment,
    };

    beforeEach(() => {
      render(<SelectEnvironment {...requiredProps} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("user can select a new environment", async () => {
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });
      const option = screen.getByRole("option", { name: optionToSelect });

      await userEvent.selectOptions(select, option);

      expect(mockedSelectEnvironment).toHaveBeenCalledWith(optionToSelect);
    });
  });
});
