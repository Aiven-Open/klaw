import { setupServer } from "msw/node";
import { rest } from "msw";

export const server = setupServer(
  rest.all("*", (req, res, ctx) => {
    console.error("Please mock all api calls in your tests.");
    console.error("Request params: ", req.params);

    return res(ctx.status(404), ctx.json({}));
  })
);
