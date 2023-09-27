import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { SetupServer } from "msw/node";
import { KlawApiResponse } from "types/utils";

function mockCreateAclRequest({
  mswInstance,
  response,
}: {
  mswInstance: SetupServer;
  response: {
    status?: number;
    data: KlawApiResponse<"createAcl"> | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/createAcl`;
  mswInstance.use(
    rest.post(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

export { mockCreateAclRequest };
