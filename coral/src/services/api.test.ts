import api, {
  AbsolutePathname,
  GenericApiResponse,
  HTTPMethod,
  isClientError,
  isServerError,
  isUnauthorizedError,
} from "src/services/api";
import { server } from "src/services/api-mocks/server";
import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";

function apiUrl(path: string) {
  return `${getHTTPBaseAPIUrl()}${path}`;
}

type HTTPScenario = {
  functionName: string;
  ok: () => Promise<unknown>;
  fakeOk: () => Promise<unknown>;
  htmlResponse: () => Promise<unknown>;
  unauthorized: () => Promise<unknown>;
  badRequest: () => Promise<unknown>;
  internalError: () => Promise<unknown>;
};

// Klaw currently does not return ERRORs from the API but always a 200
// An error is always following this patter:
// {
//   status: null,
//   timestamp: null,
//   message: null,
//   debugMessage: null,
//   result: "Failure. <SOME MORE TEXT>",
//   data: null,
// };
// to provide error messages for the user, we added a temp fix
// see `checkForFailureHiddenAsSuccess` in api.ts
// the HTTPScenario "fakeOk" is responsible for testing the fix
// until it's removed
const klawResponseResultWithHiddenError: GenericApiResponse = {
  data: {},
  timestamp: "",
  debugMessage: "",
  message: "",
  result: "Failure. A request already exists for this topic.",
  status: "200 OK",
};

describe("API client", () => {
  const mockResponseData = { foo: "bar" };
  beforeAll(() => {
    server.listen();
  });

  beforeEach(() => {
    server.use(
      rest.all(apiUrl("/ok"), async (req, res, ctx) => {
        return res.once(ctx.status(200), ctx.json(mockResponseData));
      }),

      rest.all(apiUrl("/fakeOk"), async (req, res, ctx) => {
        return res.once(
          ctx.status(200),
          ctx.json(klawResponseResultWithHiddenError)
        );
      }),
      rest.all(apiUrl("/okButHTML"), async (req, res, ctx) => {
        return res.once(
          ctx.status(200),
          ctx.set("Content-Type", "text/html"),
          ctx.text("<html></html>")
        );
      }),
      rest.all(apiUrl("/unauthorized"), async (req, res, ctx) => {
        return res.once(ctx.status(401), ctx.json(mockResponseData));
      }),
      rest.all(apiUrl("/clientError"), async (req, res, ctx) => {
        return res.once(ctx.status(400), ctx.json(mockResponseData));
      }),
      rest.all(apiUrl("/serverError"), async (req, res, ctx) => {
        return res.once(ctx.status(500), ctx.json(mockResponseData));
      })
    );
  });

  afterEach(() => {
    server.resetHandlers();
  });

  function generateScenarioForMethodWithData(
    name: HTTPMethod,
    func: (
      url: AbsolutePathname,
      data: Record<string, string>
    ) => Promise<unknown>
  ): HTTPScenario {
    const data = { not: "relevant" };
    return {
      functionName: name,
      ok: () => func("/ok", data),
      fakeOk: () => func("/fakeOk", data),
      htmlResponse: () => func("/okButHTML", data),
      unauthorized: () => func("/unauthorized", data),
      badRequest: () => func("/clientError", data),
      internalError: () => func("/serverError", data),
    };
  }

  function generateScenarioForMethod(
    name: HTTPMethod,
    func: (url: AbsolutePathname) => Promise<unknown>
  ): HTTPScenario {
    return {
      functionName: name,
      ok: () => func("/ok"),
      fakeOk: () => func("/fakeOk"),
      htmlResponse: () => func("/okButHTML"),
      unauthorized: () => func("/unauthorized"),
      badRequest: () => func("/clientError"),
      internalError: () => func("/serverError"),
    };
  }

  const GET: HTTPScenario = generateScenarioForMethod(HTTPMethod.GET, api.get);
  const POST: HTTPScenario = generateScenarioForMethodWithData(
    HTTPMethod.POST,
    api.post
  );
  const PUT: HTTPScenario = generateScenarioForMethodWithData(
    HTTPMethod.PUT,
    api.put
  );
  const PATCH: HTTPScenario = generateScenarioForMethodWithData(
    HTTPMethod.PATCH,
    api.patch
  );
  const DELETE: HTTPScenario = generateScenarioForMethod(
    HTTPMethod.DELETE,
    api.delete
  );

  [GET, POST, PUT, PATCH, DELETE].forEach(
    ({
      functionName,
      ok,
      fakeOk,
      badRequest,
      internalError,
      htmlResponse,
      unauthorized,
    }) => {
      describe(`#${functionName}`, () => {
        describe("when request is successful", () => {
          it("resolves the response payload", async () => {
            const result = await ok();
            expect(result).toEqual({ foo: "bar" });
          });
        });

        describe("When request done without authentication", () => {
          it("should throw UnauthorizedError", async () => {
            const isExpectedError = await unauthorized().catch(
              (error: unknown) => {
                if (isUnauthorizedError(error)) {
                  expect(error.status).toBe(401);
                  expect(error.statusText).toBe("Unauthorized");
                  return true;
                }
                return false;
              }
            );
            expect(isExpectedError).toBe(true);
          });
        });

        describe('when response content type is "text/html"', () => {
          it("returns value as text", async () => {
            const result = await htmlResponse();
            expect(result).toEqual("<html></html>");
          });
        });

        describe("when request fails due 4xx status code", () => {
          it("should throw ClientError", async () => {
            const isExpectedError = await badRequest().catch((error: Error) => {
              if (isClientError(error)) {
                expect(error.status).toBe(400);
                expect(error.statusText).toBe("Bad Request");
                return true;
              }
              return false;
            });
            expect(isExpectedError).toBe(true);
          });
        });

        describe("when request fails due 5xx status code", () => {
          it("should throw ServerError", async () => {
            const isExpectedError = await internalError().catch(
              (error: Error) => {
                if (isServerError(error)) {
                  expect(error.status).toBe(500);
                  expect(error.statusText).toBe("Internal Server Error");
                  return true;
                }
                return false;
              }
            );
            expect(isExpectedError).toBe(true);
          });
        });

        describe("when response is an error hidden as success", () => {
          it("should throw ClientError", async () => {
            const isExpectedError = await fakeOk().catch((error: Error) => {
              if (isClientError(error)) {
                expect(error.status).toBe(400);
                expect(error.statusText).toBe("Bad Request");
                return true;
              }
              return false;
            });
            expect(isExpectedError).toBe(true);
          });
        });
      });
    }
  );
});
