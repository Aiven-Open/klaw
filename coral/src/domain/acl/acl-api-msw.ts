import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { MswInstance } from "src/services/api-mocks/types";
import { KlawApiResponse } from "types/utils";

function mockCreateAclRequest({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<"createAclRequest"> | { message: string };
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
