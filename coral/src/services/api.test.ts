/**
 * @jest-environment node
 */

import api, {
  HTTPMethod,
  isClientError,
  isServerError,
  isUnauthorizedError,
  KlawApiError,
  KlawApiResponse,
} from "src/services/api";
import { server } from "src/services/test-utils/api-mocks/server";
import { http, HttpResponse } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { paths as ApiPaths } from "types/api";

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

const klawErrorResult: KlawApiError = {
  success: false,
  message: "This is an Error from the Klaw API",
};

describe("API client", () => {
  const mockResponseData: KlawApiResponse = {
    success: true,
    data: {},
    message: "",
  };
  beforeAll(() => {
    server.listen();
  });
  afterAll(() => {
    server.close();
  });

  beforeEach(() => {
    server.use(
      http.all(apiUrl("/ok"), async () => {
        return HttpResponse.json({ ...mockResponseData });
      }),

      http.all(apiUrl("/fakeOk"), async () => {
        return HttpResponse.json({ ...klawErrorResult });
      }),

      http.all(apiUrl("/okButHTML"), async () => {
        return new HttpResponse("<html></html>", {
          status: 200,
          headers: {
            "Content-Type": "text/html",
          },
        });
      }),
      http.all(apiUrl("/unauthorized"), async () => {
        return new HttpResponse(null, {
          status: 401,
        });
      }),
      http.all(apiUrl("/clientError"), async () => {
        return new HttpResponse(null, {
          status: 400,
        });
      }),
      http.all(apiUrl("/serverError"), async () => {
        return new HttpResponse(null, {
          status: 500,
        });
      })
    );
  });

  afterEach(() => {
    server.resetHandlers();
  });

  function generateScenarioForMethodWithData(
    name: HTTPMethod,
    func: (
      url: keyof ApiPaths,
      data: Record<string, string>
    ) => Promise<unknown>
  ): HTTPScenario {
    const data = { not: "relevant" };
    return {
      functionName: name,
      ok: () => func("/ok" as keyof ApiPaths, data),
      fakeOk: () => func("/fakeOk" as keyof ApiPaths, data),
      htmlResponse: () => func("/okButHTML" as keyof ApiPaths, data),
      unauthorized: () => func("/unauthorized" as keyof ApiPaths, data),
      badRequest: () => func("/clientError" as keyof ApiPaths, data),
      internalError: () => func("/serverError" as keyof ApiPaths, data),
    };
  }

  function generateScenarioForMethod(
    name: HTTPMethod,
    func: (pathname: keyof ApiPaths) => Promise<unknown>
  ): HTTPScenario {
    return {
      functionName: name,
      ok: () => func("/ok" as keyof ApiPaths),
      fakeOk: () => func("/fakeOk" as keyof ApiPaths),
      htmlResponse: () => func("/okButHTML" as keyof ApiPaths),
      unauthorized: () => func("/unauthorized" as keyof ApiPaths),
      badRequest: () => func("/clientError" as keyof ApiPaths),
      internalError: () => func("/serverError" as keyof ApiPaths),
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
            expect(result).toEqual(mockResponseData);
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

        describe("when response returns an KlawError", () => {
          it("should throw Klaw Error", async () => {
            const isExpectedError = await fakeOk().catch((error: Error) => {
              expect(error.message).toBe("This is an Error from the Klaw API");
              return true;
            });
            expect(isExpectedError).toBe(true);
          });
        });
      });
    }
  );
});
