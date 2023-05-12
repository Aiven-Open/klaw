import api, {
  HTTPMethod,
  // isClientError,
  // isServerError,
  // isUnauthorizedError,
  KlawApiError,
  KlawApiResponse,
} from "src/services/api";
import { server } from "src/services/api-mocks/server";
import { rest } from "msw";
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

// This whole test suite is not pleasurable to vitest :(

describe("API client", () => {
  const mockResponseData: KlawApiResponse = {
    success: true,
    data: {},
    message: "",
  };

  beforeAll(() => {
    server.listen();
    server.use(
      rest.all(apiUrl("/ok"), async (req, res, ctx) => {
        return res.once(ctx.status(200), ctx.json(mockResponseData));
      }),

      rest.all(apiUrl("/fakeOk"), async (req, res, ctx) => {
        return res.once(ctx.status(200), ctx.json(klawErrorResult));
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

  // function generateScenarioForMethodWithData(
  //   name: HTTPMethod,
  //   func: (
  //     url: keyof ApiPaths,
  //     data: Record<string, string>
  //   ) => Promise<unknown>
  // ): HTTPScenario {
  //   const data = { not: "relevant" };
  //   return {
  //     functionName: name,
  //     ok: () => func("/ok" as keyof ApiPaths, data),
  //     fakeOk: () => func("/fakeOk" as keyof ApiPaths, data),
  //     htmlResponse: () => func("/okButHTML" as keyof ApiPaths, data),
  //     unauthorized: () => func("/unauthorized" as keyof ApiPaths, data),
  //     badRequest: () => func("/clientError" as keyof ApiPaths, data),
  //     internalError: () => func("/serverError" as keyof ApiPaths, data),
  //   };
  // }

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
  // const POST: HTTPScenario = generateScenarioForMethodWithData(
  //   HTTPMethod.POST,
  //   api.post
  // );
  // const PUT: HTTPScenario = generateScenarioForMethodWithData(
  //   HTTPMethod.PUT,
  //   api.put
  // );
  // const PATCH: HTTPScenario = generateScenarioForMethodWithData(
  //   HTTPMethod.PATCH,
  //   api.patch
  // );
  // const DELETE: HTTPScenario = generateScenarioForMethod(
  //   HTTPMethod.DELETE,
  //   api.delete
  // );

  describe(`#${GET.functionName}`, () => {
    describe("when request is successful", () => {
      it("resolves the response payload", async () => {
        const result = await GET.ok();
        expect(result).toEqual(mockResponseData);
      });
    });

    // describe("When request done without authentication", () => {
    //   it("should throw UnauthorizedError", async () => {
    //     const isExpectedError = await GET.unauthorized().catch(
    //       (error: unknown) => {
    //         if (isUnauthorizedError(error)) {
    //           expect(error.status).toBe(401);
    //           expect(error.statusText).toBe("Unauthorized");
    //           return true;
    //         }
    //         return false;
    //       }
    //     );
    //     expect(isExpectedError).toBe(true);
    //   });
    // });

    // describe('when response content type is "text/html"', () => {
    //   it("returns value as text", async () => {
    //     const result = await GET.htmlResponse();
    //     expect(result).toEqual("<html></html>");
    //   });
    // });

    // describe("when request fails due 4xx status code", () => {
    //   it("should throw ClientError", async () => {
    //     const isExpectedError = await GET.badRequest().catch((error: Error) => {
    //       if (isClientError(error)) {
    //         expect(error.status).toBe(400);
    //         expect(error.statusText).toBe("Bad Request");
    //         return true;
    //       }
    //       return false;
    //     });
    //     expect(isExpectedError).toBe(true);
    //   });
    // });

    // describe("when request fails due 5xx status code", () => {
    //   it("should throw ServerError", async () => {
    //     const isExpectedError = await GET.internalError().catch(
    //       (error: Error) => {
    //         if (isServerError(error)) {
    //           expect(error.status).toBe(500);
    //           expect(error.statusText).toBe("Internal Server Error");
    //           return true;
    //         }
    //         return false;
    //       }
    //     );
    //     expect(isExpectedError).toBe(true);
    //   });
    // });

    // describe("when response returns an KlawError", () => {
    //   it("should throw Klaw Error", async () => {
    //     const isExpectedError = await GET.fakeOk().catch((error: Error) => {
    //       expect(error.message).toBe("This is an Error from the Klaw API");
    //       return true;
    //     });
    //     expect(isExpectedError).toBe(true);
    //   });
    // });
  });

  // forEach does not work in vitest
  // [GET, POST, PUT, PATCH, DELETE].forEach(
  //   ({
  //     functionName,
  //     ok,
  //     fakeOk,
  //     badRequest,
  //     internalError,
  //     htmlResponse,
  //     unauthorized,
  //   }) => {
  //     describe(`#${functionName}`, () => {
  //       describe("when request is successful", () => {
  //         it("resolves the response payload", async () => {
  //           const result = await ok();
  //           expect(result).toEqual(mockResponseData);
  //         });
  //       });

  //       describe("When request done without authentication", () => {
  //         it("should throw UnauthorizedError", async () => {
  //           const isExpectedError = await unauthorized().catch(
  //             (error: unknown) => {
  //               if (isUnauthorizedError(error)) {
  //                 expect(error.status).toBe(401);
  //                 expect(error.statusText).toBe("Unauthorized");
  //                 return true;
  //               }
  //               return false;
  //             }
  //           );
  //           expect(isExpectedError).toBe(true);
  //         });
  //       });

  //       describe('when response content type is "text/html"', () => {
  //         it("returns value as text", async () => {
  //           const result = await htmlResponse();
  //           expect(result).toEqual("<html></html>");
  //         });
  //       });

  //       describe("when request fails due 4xx status code", () => {
  //         it("should throw ClientError", async () => {
  //           const isExpectedError = await badRequest().catch((error: Error) => {
  //             if (isClientError(error)) {
  //               expect(error.status).toBe(400);
  //               expect(error.statusText).toBe("Bad Request");
  //               return true;
  //             }
  //             return false;
  //           });
  //           expect(isExpectedError).toBe(true);
  //         });
  //       });

  //       describe("when request fails due 5xx status code", () => {
  //         it("should throw ServerError", async () => {
  //           const isExpectedError = await internalError().catch(
  //             (error: Error) => {
  //               if (isServerError(error)) {
  //                 expect(error.status).toBe(500);
  //                 expect(error.statusText).toBe("Internal Server Error");
  //                 return true;
  //               }
  //               return false;
  //             }
  //           );
  //           expect(isExpectedError).toBe(true);
  //         });
  //       });

  //       describe("when response returns an KlawError", () => {
  //         it("should throw Klaw Error", async () => {
  //           const isExpectedError = await fakeOk().catch((error: Error) => {
  //             expect(error.message).toBe("This is an Error from the Klaw API");
  //             return true;
  //           });
  //           expect(isExpectedError).toBe(true);
  //         });
  //       });
  //     });
  //   }
  // );
});
