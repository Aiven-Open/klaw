import { setupServer } from "msw/node";
import { rest } from "msw";

export const server = setupServer(
  rest.all("*", (req, res, ctx) => {
    console.error(
      `
A test has made an API call which was not caught by a mock (endpoint: ${req.url}). 
Please make sure that every test file is mocking the appropriate API endpoints. 
Some page components may render child components sending those call.
`
    );

    return res(ctx.status(404), ctx.json({}));
  })
);
