import { cleanup, screen, within, render } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TeamsTable } from "src/app/features/configuration/teams/components/TeamsTable";
import { Team } from "src/domain/team";

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

const mockTeams = [teamOne, teamTwo];
const tableRowHeader = [
  "Team name",
  "Team email",
  "Team phone",
  "Contact person",
  "Tenant",
];

describe("TeamsTable.tsx", () => {
  describe("handles empty state", () => {
    beforeAll(() => {
      render(<TeamsTable teams={[]} />);
    });
    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      const heading = screen.getByRole("heading", { name: "No teams" });
      expect(heading).toBeVisible();
    });

    it("show additional information when there is no data", () => {
      const text = screen.getByText("There are no teams data available.");
      expect(text).toBeVisible();
    });
  });

  describe("shows all teams data as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<TeamsTable teams={mockTeams} />);
    });

    afterAll(cleanup);

    it("renders a team table", async () => {
      const table = screen.getByRole("table", {
        name: "Teams overview",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockTeams.forEach((team) => {
      it(`renders the team name "${team.teamname}"`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const cell = within(table).getByRole("cell", {
          name: team.teamname,
        });

        expect(cell).toBeVisible();
      });

      if (team.teammail) {
        it(`renders the team email if available`, () => {
          const table = screen.getByRole("table", {
            name: "Teams overview",
          });
          const cell = within(table).getByRole("cell", {
            name: team.teammail,
          });

          expect(cell).toBeVisible();
        });
      }

      it(`renders the team phone`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const cell = within(table).getByRole("cell", {
          name: team.teamname,
        });

        expect(cell).toBeVisible();
      });

      it(`renders the contact person`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const cell = within(table).getByRole("cell", {
          name: team.contactperson,
        });

        expect(cell).toBeVisible();
      });

      it(`renders the team tenant`, () => {
        const table = screen.getByRole("table", {
          name: "Teams overview",
        });
        const cell = within(table).getByRole("cell", {
          name: team.tenantName,
        });

        expect(cell).toBeVisible();
      });
    });
  });
});
