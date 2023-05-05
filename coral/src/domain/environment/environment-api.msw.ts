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

function mockGetEnvironmentsForTopicRequest({
  mswInstance,
  response,
}: MockApi<"getEnvsBaseCluster">) {
  const url = `${getHTTPBaseAPIUrl()}/getEnvsBaseCluster`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

function mockGetEnvironmentsForTeam({
  mswInstance,
  response,
}: MockApi<"getEnvsBaseClusterFilteredForTeam">) {
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

interface GetClusterInfoFromEnvRequestArgs {
  mswInstance: MswInstance;
  response: KlawApiResponse<"getClusterInfoFromEnv">;
}

function mockGetClusterInfoFromEnv({
  mswInstance,
  response,
}: GetClusterInfoFromEnvRequestArgs) {
  const url = `${getHTTPBaseAPIUrl()}/getClusterInfoFromEnv`;

  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(response));
    })
  );
}

const getMockedResponseGetClusterInfoFromEnv = (
  isAivenCluster: boolean
): KlawApiResponse<"getClusterInfoFromEnv"> => ({
  aivenCluster: isAivenCluster,
});

export {
  mockGetEnvironmentsForTopicRequest,
  mockGetEnvironmentsForTeam,
  mockedEnvironmentResponse,
  mockGetClusterInfoFromEnv,
  getMockedResponseGetClusterInfoFromEnv,
};
