import { cleanup, screen, within } from "@testing-library/react";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TeamsOverview } from "src/app/features/user-information/profile/components/TeamsOverview";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const testTeams = ["Discovery", "Enterprise", "Voyager"];
describe("TeamsOverview.tsx", () => {
  describe("shows a table with all teams", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      customRender(<TeamsOverview teams={testTeams} />, { memoryRouter: true });
    });

    afterAll(cleanup);

    it("shows a headline", () => {
      const headline = screen.getByRole("heading", { name: "Teams" });

      expect(headline).toBeVisible();
    });

    it("shows a table with accessible name", () => {
      const table = screen.getByRole("table", {
        name: "Teams user belongs to",
      });

      expect(table).toBeVisible();
    });

    it("shows a column header for team name", () => {
      const table = screen.getByRole("table", {
        name: "Teams user belongs to",
      });
      const columnHeader = within(table).getByRole("columnheader", {
        name: "Team name",
      });

      expect(columnHeader).toBeVisible();
    });

    it("shows a for for each team", () => {
      const table = screen.getByRole("table", {
        name: "Teams user belongs to",
      });
      const rows = within(table).getAllByRole("row");

      // one row is the header row
      const rowLength = testTeams.length + 1;
      expect(rows).toHaveLength(rowLength);
    });

    testTeams.forEach((team) => {
      it(`shows a row for ${team}`, () => {
        const table = screen.getByRole("table", {
          name: "Teams user belongs to",
        });

        const row = within(table).getByRole("row", { name: team });
        expect(row).toBeVisible();
      });
    });

    it("shows a link to the page showing all teams", () => {
      const link = screen.getByRole("link", {
        name: "See all teams",
      });

      expect(link).toBeVisible();
    });
  });
});
