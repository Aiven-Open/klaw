import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SelectEnv from "src/app/features/topics/components/select-env/SelectEnv";
import { Environment } from "src/domain/environment";

describe("SelectEnv.tsx", () => {
  const envOptions = ["ALL", "DEV", "TST"].map((env) => ({
    label: env,
    value: env,
  }));

  describe("renders all necessary elements", () => {
    const activeOption: Environment = "ALL";

    const requiredProps = {
      envOptions,
      activeOption,
      selectEnv: jest.fn(),
    };

    beforeAll(() => {
      render(<SelectEnv {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a select element for Kafka environments", () => {
      const select = screen.getByRole("combobox", {
        name: "Kafka Environment",
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of given options for environments", () => {
      envOptions.forEach(({ label }) => {
        const option = screen.getByRole("option", {
          name: label,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(envOptions.length);
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

    const mockedSelectEnv = jest.fn();
    const requiredProps = {
      envOptions,
      activeOption: "ALL",
      selectEnv: mockedSelectEnv,
    };

    beforeEach(() => {
      render(<SelectEnv {...requiredProps} />);
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

      expect(mockedSelectEnv).toHaveBeenCalledWith(optionToSelect);
    });
  });
});
