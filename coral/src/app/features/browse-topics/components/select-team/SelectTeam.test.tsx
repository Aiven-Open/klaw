import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SelectTeam from "src/app/features/browse-topics/components/select-team/SelectTeam";
import { server } from "src/services/api-mocks/server";
import { mockedTeamResponse, mockGetTeams } from "src/domain/team/team-api.msw";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";

describe("SelectTeam.tsx", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("renders default view when no query is set", () => {
    const mockedOnChange = jest.fn();

    beforeAll(async () => {
      mockGetTeams({ mswInstance: server });
      customRender(<SelectTeam onChange={mockedOnChange} />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-team-loading")
      );
    });

    afterAll(cleanup);

    it("shows a select element for Team", () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of given options for teams plus a option for `All teams`", () => {
      mockedTeamResponse.forEach((team) => {
        const option = screen.getByRole("option", {
          name: team,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(
        mockedTeamResponse.length + 1
      );
    });

    it("shows `All teams` as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All teams");
    });

    it("updates the team state for initial api call with value associated with `All teams`", () => {
      expect(mockedOnChange).toHaveBeenCalledWith(ALL_TEAMS_VALUE);
    });
  });

  describe("sets the active team based on a query param", () => {
    const mockedOnChange = jest.fn();

    const teamQueryValue = "TEST_TEAM_02";

    beforeEach(async () => {
      const routePath = "/topics?team=TEST_TEAM_02";

      mockGetTeams({ mswInstance: server });

      customRender(<SelectTeam onChange={mockedOnChange} />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-team-loading")
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("shows `TEST_TEAM_02` as the active option one", async () => {
      const option = await screen.findByRole("option", {
        name: teamQueryValue,
        selected: true,
      });
      expect(option).toBeVisible();
    });

    it("updates the team state for api call", () => {
      expect(mockedOnChange).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles user selecting a team", () => {
    const mockedOnChange = jest.fn();
    const optionToSelect = "TEST_TEAM_02";

    beforeEach(async () => {
      mockGetTeams({ mswInstance: server });
      customRender(<SelectTeam onChange={mockedOnChange} />, {
        queryClient: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-team-loading")
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("updates state for api call when user selects a new team", async () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });
      const option = screen.getByRole("option", { name: optionToSelect });

      await userEvent.selectOptions(select, option);

      expect(mockedOnChange).toHaveBeenCalledWith(optionToSelect);
    });

    it("sets the team the user choose as active option", async () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });
      const option = screen.getByRole("option", { name: optionToSelect });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(optionToSelect);
    });
  });

  describe("updates the search param to preserve team in url", () => {
    const mockedOnChange = jest.fn();
    const optionToSelect = "TEST_TEAM_01";

    beforeEach(async () => {
      mockGetTeams({ mswInstance: server });
      customRender(<SelectTeam onChange={mockedOnChange} />, {
        queryClient: true,
        browserRouter: true,
      });
      await waitFor(() => {
        expect(screen.getByRole("combobox")).toBeVisible();
      });
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      jest.resetAllMocks();
      cleanup();
    });

    it("shows no search param by default", async () => {
      expect(window.location.search).toEqual("");
    });

    it("sets `TEST_TEAM_01` as search param when user selected it", async () => {
      const select = screen.getByRole("combobox", {
        name: "Team",
      });

      const option = screen.getByRole("option", { name: optionToSelect });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(`?team=${optionToSelect}`);
      });
    });
  });
});
