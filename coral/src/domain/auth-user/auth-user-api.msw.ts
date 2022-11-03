import { rest } from "msw";
import { AuthUser } from "src/domain/auth-user/auth-user-types";
import { MswInstance } from "src/services/api-mocks/types";

function mockUserAuthRequest({
  mswInstance,
  userResponse,
}: {
  mswInstance: MswInstance;
  userResponse: AuthUser;
}) {
  mswInstance.use(
    rest.post("/user/authenticate", (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(userResponse));
    })
  );
}

export { mockUserAuthRequest };
