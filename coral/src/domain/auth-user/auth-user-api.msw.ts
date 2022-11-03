import { rest } from "msw";
import { AuthUser } from "src/domain/auth-user/auth-user-types";
import { MswInstance } from "src/domain/api-mocks/types";

function mockUserAuthRequest({
  mswInstance,
  userObject,
}: {
  mswInstance: MswInstance;
  userObject: AuthUser;
}) {
  mswInstance.use(
    rest.post("/user/authenticate", (req, res, ctx) => {
      sessionStorage.setItem("is-authenticated", "true");

      return res(ctx.status(200), ctx.json(userObject));
    })
  );
}

export { mockUserAuthRequest };
