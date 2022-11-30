import { MswInstance } from "src/services/api-mocks/types";
import { rest } from "msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";

function mockGetEnvironments({
  mswInstance,
  scenario,
}: {
  mswInstance: MswInstance;
  scenario?: "error";
}) {
  const url = `${getHTTPBaseAPIUrl()}/getEnvs`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      if (scenario === "error") {
        return res(ctx.status(400), ctx.json(""));
      }

      return res(
        ctx.status(200),
        ctx.json([
          createMockEnvironmentDTO({ name: "DEV", id: "1" }),
          createMockEnvironmentDTO({ name: "TST", id: "2" }),
        ])
      );
    })
  );
}

export { mockGetEnvironments };
