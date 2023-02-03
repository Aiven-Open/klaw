import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { MswInstance } from "src/services/api-mocks/types";
import { KlawApiResponse } from "types/utils";

function mockGetNotificationCounts({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<"getAuth"> | { message: string };
  };
}) {
  mswInstance.use(
    rest.get(`${getHTTPBaseAPIUrl()}/getAuth`, async (req, res, ctx) =>
      res(ctx.status(response.status ?? 200), ctx.json(response.data))
    )
  );
}

export { mockGetNotificationCounts };
