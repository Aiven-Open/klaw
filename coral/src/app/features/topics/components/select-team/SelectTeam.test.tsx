import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SelectTeam from "src/app/features/topics/components/select-team/SelectTeam";

describe("SelectTeam.tsx", () => {
  const teamOptions = ["All teams", "Marketing", "Infra", "Devrel"];

  describe("renders all necessary elements", () => {
    const activeOption = "Marketing";

    const requiredProps = {
      teamOptions,
      activeOption,
      selectTeam: jest.fn(),
    };

    beforeAll(() => {
      render(<SelectTeam {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a select element for Team", () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of given options for teams", () => {
      teamOptions.forEach((env) => {
        const option = screen.getByRole("option", {
          name: env,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(teamOptions.length);
    });

    it("shows a given team as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName(activeOption);
    });
  });

  describe("handles the change event for selecting", () => {
    const optionToSelect = "Devrel";

    const mockedSelectTeam = jest.fn();
    const requiredProps = {
      teamOptions,
      activeOption: "Infra",
      selectTeam: mockedSelectTeam,
    };

    beforeEach(() => {
      render(<SelectTeam {...requiredProps} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("user can select a new team", async () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });
      const option = screen.getByRole("option", { name: optionToSelect });

      await userEvent.selectOptions(select, option);

      expect(mockedSelectTeam).toHaveBeenCalledWith(optionToSelect);
    });
  });
});
