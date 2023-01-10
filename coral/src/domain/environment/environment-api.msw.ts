import { MswInstance } from "src/services/api-mocks/types";
import { rest } from "msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { getHTTPBaseAPIUrl } from "src/config";
import { KlawApiResponse } from "types/utils";
import { operations } from "types/api";

type MockApi<T extends keyof operations> = {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<T> | { message: string };
  };
};

function mockGetEnvironments({
  mswInstance,
  response,
}: MockApi<"environmentsGet">) {
  const url = `${getHTTPBaseAPIUrl()}/getEnvs`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

function mockGetEnvironmentsForTeam({
  mswInstance,
  response,
}: MockApi<"envsBaseClusterFilteredForTeamGet">) {
  const url = `${getHTTPBaseAPIUrl()}/getEnvsBaseClusterFilteredForTeam`;
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
export {
  mockGetEnvironments,
  mockGetEnvironmentsForTeam,
  mockedEnvironmentResponse,
};
