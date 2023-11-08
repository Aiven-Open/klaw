import { getTeams, Team } from "src/domain/team";
import { Teams } from "src/app/features/configuration/teams/Teams";
import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

jest.mock("src/domain/team/team-api");

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

const teamOne: Team = {
  app: "",
  contactperson: "Picard",
  envList: [],
  serviceAccounts: {},
  showDeleteTeam: false,
  teamId: 1111,
  teammail: "",
  teamname: "NCC-1701-D",
  teamphone: "12345",
  tenantId: 0,
  tenantName: "UFP",
};
const teamTwo: Team = {
  contactperson: "Pike",
  showDeleteTeam: false,
  teamId: 2222,
  teamname: "NCC-1701",
  teamphone: "67890",
  tenantId: 0,
  tenantName: "default",
};

const mockedTeams = [teamOne, teamTwo];

describe("Teams.tsx", () => {
  describe("handles loading state", () => {
    beforeAll(() => {
      mockGetTeams.mockResolvedValue([]);
      customRender(<Teams />, { queryClient: true });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a loading information", () => {
      const loadingAnimation = screen.getByTestId("skeleton-table");
      expect(loadingAnimation).toBeVisible();
    });
  });

  describe("handles empty state", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      customRender(<Teams />, { queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();
    });

    it("shows information about the empty state", () => {
      const heading = screen.getByRole("heading", { name: "No teams" });
      expect(heading).toBeVisible();
    });
  });

  describe("handles error state", () => {
    const testError: KlawApiError = {
      message: "OH NO 😭",
      success: false,
    };

    const originalConsoleError = console.error;

    beforeAll(async () => {
      console.error = jest.fn();
      mockGetTeams.mockRejectedValue(testError);
      customRender(<Teams />, { queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
      console.error = originalConsoleError;
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalled();
    });

    it("shows an error alert", () => {
      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testError.message);
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });

  describe("handles rendering of team data", () => {
    beforeAll(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue(mockedTeams);
      customRender(<Teams />, { queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a table with team overview", () => {
      const table = screen.getByRole("table", {
        name: "Teams overview",
      });

      expect(table).toBeVisible();
    });

    mockedTeams.forEach((team) => {
      it(`renders the team name "${team.teamname}"`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const cell = within(table).getByRole("cell", {
          name: team.teamname,
        });

        expect(cell).toBeVisible();
      });
    });
  });
});
