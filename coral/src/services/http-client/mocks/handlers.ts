import { rest } from "msw";
import { AuthUser } from "src/domain/auth-user";

const AUTH_USER_API_PATH = "/user/authenticate";
export const handlers = [
  rest.post(AUTH_USER_API_PATH, (req, res, ctx) => {
    sessionStorage.setItem("is-authenticated", "true");

    const user: AuthUser = {
      name: "Super Admin",
    };
    return res(ctx.status(200), ctx.json(user));
  }),
];
