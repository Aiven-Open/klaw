import { rest } from "msw";
import { AuthUser } from "src/domain/auth-user/auth-user-types";

// @TODO check right type for MSW instance
function mockUserAuthRequest({
  mswInstance,
  userObject,
}: {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  mswInstance: any;
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
