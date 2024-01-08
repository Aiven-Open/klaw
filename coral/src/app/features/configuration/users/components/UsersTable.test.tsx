import { cleanup, screen, render, within } from "@testing-library/react";
import { UsersTable } from "src/app/features/configuration/users/components/UsersTable";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { User } from "src/domain/user";

const userOne: User = {
  fullname: "Jean-Luc Picard",
  mailid: "jlpicard@ufp.org",
  role: "CAPTAIN",
  switchTeams: false,
  team: "Enterprise",
  teamId: 1,
  tenantId: 0,
  username: "js-picard",
};

const userTwo: User = {
  fullname: "Chris Pike",
  mailid: "cpike@ufp.org",
  role: "SUPERADMIN",
  switchAllowedTeamIds: [1, 2],
  switchAllowedTeamNames: ["Enterprise", "Discovery"],
  switchTeams: true,
  team: "Discovery",
  teamId: 2,
  tenantId: 0,
  username: "c-pike",
};

const mockUsers = [userOne, userTwo];

const tableRowHeader = [
  "Username",
  "Name",
  "Role",
  "Team",
  "Email ID",
  "Switch teams",
  "Switch between teams",
];

const testLabel = "Users overview, page 1 of 1";
describe("UsersTable.tsx", () => {
  describe("handles empty state", () => {
    beforeAll(() => {
      render(<UsersTable users={[]} ariaLabel={testLabel} />);
    });
    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      const heading = screen.getByRole("heading", { name: "No users" });
      expect(heading).toBeVisible();
    });

    it("show additional information when there is no data", () => {
      const text = screen.getByText("There are no users data available.");
      expect(text).toBeVisible();
    });
  });

  describe("shows all users as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(<UsersTable users={mockUsers} ariaLabel={testLabel} />);
    });

    afterAll(cleanup);

    it("renders a users table", async () => {
      const table = screen.getByRole("table", {
        name: testLabel,
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockUsers.forEach((user) => {
      it(`renders the user name "${user.username}"`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });
        const cell = within(table).getByRole("cell", {
          name: user.username,
        });

        expect(cell).toBeVisible();
      });

      it(`renders the users full name "${user.fullname}"`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });
        const cell = within(table).getByRole("cell", {
          name: user.fullname,
        });

        expect(cell).toBeVisible();
      });

      it(`renders the users team "${user.team}"`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });
        const cell = within(table).getByRole("cell", {
          name: user.team,
        });

        expect(cell).toBeVisible();
      });

      it(`renders the users email ID "${user.mailid}"`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });
        const cell = within(table).getByRole("cell", {
          name: user.mailid,
        });

        expect(cell).toBeVisible();
      });

      it(`renders indicator that user can switch team`, () => {
        const table = screen.getByRole("table", {
          name: testLabel,
        });

        const row = within(table).getByRole("row", {
          name: new RegExp(`${user.username}`, "i"),
        });
        const cell = within(row).getAllByRole("cell");

        const positionOfSwitchTeamRow = tableRowHeader.indexOf("Switch teams");
        expect(cell[positionOfSwitchTeamRow]).toHaveTextContent(
          user.switchTeams ? "Enabled" : "Disabled"
        );
      });

      if (user.switchAllowedTeamNames) {
        it(`renders all teams a user can switch between if its enabled for them`, () => {
          const table = screen.getByRole("table", {
            name: testLabel,
          });

          const row = within(table).getByRole("row", {
            name: new RegExp(`${user.username}`, "i"),
          });
          const cell = within(row).getAllByRole("cell");

          const positionOfTeamsToSwitch = tableRowHeader.indexOf(
            "Switch between teams"
          );

          user.switchAllowedTeamNames?.forEach((name) => {
            expect(cell[positionOfTeamsToSwitch]).toHaveTextContent(name);
          });
        });
      }
    });
  });
});
