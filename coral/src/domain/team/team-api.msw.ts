import { rest } from "msw";
import { MswInstance } from "src/services/api-mocks/types";

function mockGetTeams({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error";
}) {
  mswInstance.use(
    rest.get("getAllTeamsSUOnly", async (req, res, ctx) => {
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }

      return res(
        ctx.status(200),
        ctx.json(["TEST_TEAM_01", "TEST_TEAM_02", "TEST_TEAM_03"])
      );
    })
  );
}

export { mockGetTeams };
