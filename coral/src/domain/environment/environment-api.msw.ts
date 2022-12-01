import { MswInstance } from "src/services/api-mocks/types";
import { rest } from "msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";
import { EnvironmentsGetResponse } from "src/domain/environment/environment-types";

function mockGetEnvironments({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: EnvironmentsGetResponse | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/getEnvs`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

const mockedEnvironmentResponse = [
  createMockEnvironmentDTO({ name: "DEV", id: "1" }),
  createMockEnvironmentDTO({ name: "TST", id: "2" }),
];
export { mockGetEnvironments, mockedEnvironmentResponse };
