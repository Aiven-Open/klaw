import { rest } from "msw";

const AUTH_USER_API_PATH = "/user/authenticate";
export const handlers = [
  rest.post(AUTH_USER_API_PATH, (req, res, ctx) => {
    sessionStorage.setItem("is-authenticated", "true");

    return res(
      ctx.status(200),
      ctx.json({
        username: "Super Admin",
      })
    );
  }),

  rest.get("/user", (req, res, ctx) => {
    const isAuthenticated = sessionStorage.getItem("is-authenticated");

    if (!isAuthenticated) {
      return res(
        ctx.status(403),
        ctx.json({
          errorMessage: "Not authorized",
        })
      );
    }

    return res(
      ctx.status(200),
      ctx.json({
        username: "Super Admin",
      })
    );
  }),
];
