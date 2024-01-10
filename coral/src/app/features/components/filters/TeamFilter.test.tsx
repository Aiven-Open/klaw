import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { userEvent } from "@testing-library/user-event";
import TeamFilter from "src/app/features/components/filters/TeamFilter";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { getTeams } from "src/domain/team/team-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiError } from "src/services/api";

jest.mock("src/domain/team/team-api");

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

const mockedTeamsResponse = [
  {
    teamname: "Ospo",
    teammail: "ospo@aiven.io",
    teamphone: "003157843623",
    contactperson: "Ospo Office",
    tenantId: 101,
    teamId: 1003,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
  {
    teamname: "DevRel",
    teammail: "devrel@aiven.io",
    teamphone: "003146237478",
    contactperson: "Dev Rel",
    tenantId: 101,
    teamId: 1004,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
];

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const WrappedTeamFilter = withFiltersContext({
  element: <TeamFilter />,
});

const WrappedTeamFilterForTeamName = withFiltersContext({
  element: <TeamFilter useTeamName={true} />,
});

const filterLabel = "Filter by team";
describe("TeamFilter.tsx", () => {
  describe("handles the teamId per default", () => {
    describe("renders default view when no query is set", () => {
      beforeAll(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilter />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterAll(cleanup);

      it("shows a select element for Team", () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });

        expect(select).toBeEnabled();
      });

      it("renders a list of given options for teams plus a option for `All teams`", () => {
        mockedTeamsResponse.forEach((team) => {
          const option = screen.getByRole("option", {
            name: team.teamname,
          });

          expect(option).toBeEnabled();
        });
        expect(screen.getAllByRole("option")).toHaveLength(
          mockedTeamsResponse.length + 1
        );
      });

      it("shows `All teams` as the active option one", () => {
        const option = screen.getByRole("option", {
          selected: true,
        });
        expect(option).toHaveAccessibleName("All teams");
      });
    });

    describe("sets the active team based on a query param", () => {
      const optionName = "Ospo";
      const optionId = "1003";

      beforeEach(async () => {
        const routePath = `/topics?teamId=${optionId}`;

        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilter />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
          customRoutePath: routePath,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it("shows `Ospo` as the active option one", async () => {
        const option = await screen.findByRole("option", {
          name: optionName,
          selected: true,
        });
        expect(option).toBeVisible();
      });
    });

    describe("handles user selecting a team", () => {
      const optionToSelect = "Ospo";
      const optionId = "1003";

      beforeEach(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilter />, {
          queryClient: true,
          aquariumContext: true,
          memoryRouter: true,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it("sets the team the user choose as active option", async () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });
        const option = screen.getByRole("option", { name: optionToSelect });

        await userEvent.selectOptions(select, option);

        expect(select).toHaveValue(optionId);
        expect(select).toHaveDisplayValue(optionToSelect);
      });
    });

    describe("updates the search param to preserve team in url", () => {
      const optionToSelect = "DevRel";
      const optionId = "1004";

      beforeEach(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);
        customRender(<WrappedTeamFilter />, {
          queryClient: true,
          aquariumContext: true,
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

      it("sets `teamId=1` and `page=1` as search param when user selected it", async () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });

        const option = screen.getByRole("option", { name: optionToSelect });

        await userEvent.selectOptions(select, option);

        await waitFor(() => {
          expect(window.location.search).toEqual(`?teamId=${optionId}&page=1`);
        });
      });
    });
  });

  describe("handles the teamName optional instead of id", () => {
    describe("renders default view when no query is set", () => {
      beforeAll(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilterForTeamName />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterAll(cleanup);

      it("shows a select element for Team", () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });

        expect(select).toBeEnabled();
      });

      it("renders a list of given options for teams plus a option for `All teams`", () => {
        mockedTeamsResponse.forEach((team) => {
          const option = screen.getByRole("option", {
            name: team.teamname,
          });

          expect(option).toBeEnabled();
        });
        expect(screen.getAllByRole("option")).toHaveLength(
          mockedTeamsResponse.length + 1
        );
      });

      it("shows `All teams` as the active option one", () => {
        const option = screen.getByRole("option", {
          selected: true,
        });
        expect(option).toHaveAccessibleName("All teams");
      });
    });

    describe("sets the active team based on a query param", () => {
      const optionName = "Ospo";

      beforeEach(async () => {
        const routePath = `/topics?teamName=${optionName}`;

        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilterForTeamName />, {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
          customRoutePath: routePath,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it("shows `Ospo` as the active option one", async () => {
        const option = await screen.findByRole("option", {
          name: optionName,
          selected: true,
        });
        expect(option).toBeVisible();
      });
    });

    describe("handles user selecting a team", () => {
      const optionToSelect = "Ospo";

      beforeEach(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);

        customRender(<WrappedTeamFilterForTeamName />, {
          queryClient: true,
          aquariumContext: true,
          memoryRouter: true,
        });
        await waitForElementToBeRemoved(
          screen.getByTestId("async-select-loading")
        );
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it("sets the team the user choose as active option", async () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });
        const option = screen.getByRole("option", { name: optionToSelect });

        await userEvent.selectOptions(select, option);

        expect(select).toHaveValue(optionToSelect);
        expect(select).toHaveDisplayValue(optionToSelect);
      });
    });

    describe("updates the search param to preserve team name in url", () => {
      const optionToSelect = "DevRel";

      beforeEach(async () => {
        mockGetTeams.mockResolvedValue(mockedTeamsResponse);
        customRender(<WrappedTeamFilterForTeamName />, {
          queryClient: true,
          aquariumContext: true,
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

      it("sets `teamName=DevRel` and `page=1` as search param when user selected it", async () => {
        const select = screen.getByRole("combobox", {
          name: filterLabel,
        });

        const option = screen.getByRole("option", { name: optionToSelect });

        await userEvent.selectOptions(select, option);

        await waitFor(() => {
          expect(window.location.search).toEqual(
            `?teamName=${optionToSelect}&page=1`
          );
        });
      });
    });
  });

  describe("gives user information if fetching teams failed", () => {
    const testError: KlawApiError = {
      message: "Oh no, this did not work",
      success: false,
    };

    const originalConsoleError = jest.fn();

    beforeEach(async () => {
      console.error = jest.fn();
      mockGetTeams.mockRejectedValue(testError);
      customRender(<WrappedTeamFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading")
      );
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a disabled select with No teams", () => {
      const select = screen.getByRole("combobox", {
        name: "No Teams",
      });

      expect(select).toBeDisabled();
    });

    it("shows an error message below the select element", () => {
      const errorText = screen.getByText("Teams could not be loaded.");

      expect(errorText).toBeVisible();
    });

    it("shows a toast notification with error", () => {
      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: `Error loading Teams: ${testError.message}`,
        })
      );
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });
});
