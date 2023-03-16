import { MswInstance } from "src/services/api-mocks/types";
import { rest } from "msw";
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

function mockCreateSchemaRequest({
  mswInstance,
  response,
}: MockApi<"uploadSchema">) {
  const url = `${getHTTPBaseAPIUrl()}/uploadSchema`;
  mswInstance.use(
    rest.post(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

export { mockCreateSchemaRequest };
