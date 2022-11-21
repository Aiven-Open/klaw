import { MswInstance } from "src/services/api-mocks/types";
import { rest } from "msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";

function mockGetEnvironments({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error";
}) {
  mswInstance.use(
    rest.get("getEnvs", async (req, res, ctx) => {
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }

      return res(
        ctx.status(200),
        ctx.json([
          createMockEnvironmentDTO("DEV"),
          createMockEnvironmentDTO("TST"),
        ])
      );
    })
  );
}

export { mockGetEnvironments };
