import { setupServer } from "msw/node";
import { http, HttpResponse } from "msw";

export const server = setupServer(
  http.all("*", ({ request }) => {
    console.error(
      `A test has made an API call which was not caught by a mock (endpoint: ${request.url}).
       Please make sure that every test file is mocking the appropriate API endpoints.
       Some page components may render child components sending those call.`
    );

    return new HttpResponse("Non existent endpoint", {
      status: 404,
    });
  })
);
