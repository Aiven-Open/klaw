import { rest } from "msw";
import { AuthUser } from "src/domain/auth-user/auth-user-types";
import { MswInstance } from "src/services/api-mocks/types";

const user: AuthUser = {
  name: "Super Admin",
};

const correctUsername = "superadmin";

function mockUserAuthRequest(mswInstance: MswInstance) {
  mswInstance.use(
    rest.post("/user/authenticate", async (req, res, ctx) => {
      const { username } = await req.json();

      // hard coded check for the "right" username to simulate error case
      // remove when real api is connected
      if (username !== correctUsername) {
        return res(
          ctx.status(404),
          ctx.json({
            errorMessage: `wrong username`,
          })
        );
      }
      return res(ctx.status(200), ctx.json(user));
    })
  );
}

export { mockUserAuthRequest, correctUsername };
