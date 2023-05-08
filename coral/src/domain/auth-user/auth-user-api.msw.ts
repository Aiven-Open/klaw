import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { AuthUser } from "src/domain/auth-user/auth-user-types";
import { MswInstance } from "src/services/api-mocks/types";

const user: AuthUser = {
  username: "Super Admin",
  teamname: "",
  teamId: "",
  canSwitchTeams: "",
};

const correctUsername = "superadmin";

function mockUserAuthRequest(mswInstance: MswInstance) {
  mswInstance.use(
    rest.post(`${getHTTPBaseAPIUrl()}/login`, async (req, res, ctx) => {
      const responseText = await req.text();
      const data = new URLSearchParams(responseText);
      const username = data.get("username");
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
