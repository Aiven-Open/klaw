import { getHTTPBaseAPIUrl } from "src/config";
import isPlainObject from "lodash/isPlainObject";
import { components } from "types/api";
import { objectHasProperty } from "src/services/type-utils";

type GenericApiResponse = components["schemas"]["GenericApiResponse"];

enum HTTPMethod {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
  PATCH = "PATCH",
  DELETE = "DELETE",
}
type SomeObject =
  | Record<string, unknown>
  | Record<string, never>
  | Array<unknown>;
type AbsolutePathname = `/${string}`;
const CONTENT_TYPE_JSON = "application/json" as const;

const API_BASE_URL = getHTTPBaseAPIUrl();

type HTTPError = {
  status: number;
  statusText: string;
  data: SomeObject | string;
  headers: Headers;
};

type UnauthorizedError = HTTPError & { status: 401 };
type ClientError = HTTPError & {
  status:
    | 400
    | 402
    | 403
    | 404
    | 405
    | 406
    | 407
    | 408
    | 409
    | 410
    | 411
    | 412
    | 413
    | 414
    | 415
    | 416
    | 417
    | 418
    | 421
    | 422
    | 423
    | 424
    | 426
    | 428
    | 429
    | 431
    | 444
    | 451
    | 499;
};
type ServerError = HTTPError & {
  status: 500 | 501 | 502 | 503 | 504 | 505 | 506 | 507 | 508 | 510 | 511 | 599;
};

function hasHTTPErrorProperties(
  value: Record<string, unknown>
): value is Record<keyof HTTPError, unknown> {
  return (
    "status" in value &&
    "statusText" in value &&
    "data" in value &&
    "headers" in value
  );
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return isPlainObject(value);
}

function isHTTPError(value: unknown): value is HTTPError {
  if (isRecord(value) && hasHTTPErrorProperties(value)) {
    return (
      typeof value.status === "number" &&
      typeof value.statusText === "string" &&
      (typeof value.data === "string" || typeof value.data === "object")
    );
  }
  return false;
}

function isUnauthorizedError(value: unknown): value is UnauthorizedError {
  return isHTTPError(value) && value.status === 401;
}

function isClientError(value: unknown): value is ClientError {
  return isHTTPError(value) && value.status >= 400 && value.status < 500;
}

function isServerError(value: unknown): value is ServerError {
  return isHTTPError(value) && value.status >= 500;
}

function isRedirectToPathname(response: Response, pathname: string): boolean {
  if (response.redirected && response.ok) {
    const responseURL = new URL(response.url);
    return responseURL.pathname === pathname;
  }
  return false;
}

function transformHTTPRedirectToLoginTo401(response: Response): Response {
  if (isRedirectToPathname(response, "/login")) {
    return new Response("Unauthorized", {
      status: 401,
      statusText: "Unauthorized",
    });
  }
  return response;
}

function transformHTTPRedirectToRootTo204(response: Response): Response {
  if (isRedirectToPathname(response, "/")) {
    return new Response(null, {
      status: 204,
      statusText: "No Content",
      headers: response.headers,
    });
  }
  return response;
}

function withFormPayload(data: URLSearchParams): Partial<RequestInit> {
  return {
    body: data.toString(),
    headers: {
      accept: CONTENT_TYPE_JSON,
      "content-type": "application/x-www-form-urlencoded",
    },
  };
}

function withJSONPayload<TBody extends SomeObject>(
  data: TBody
): Partial<RequestInit> {
  return {
    body: JSON.stringify(data),
    headers: {
      accept: CONTENT_TYPE_JSON,
      "content-type": CONTENT_TYPE_JSON,
    },
  };
}

function withPayload<TBody extends SomeObject | URLSearchParams>(
  method: HTTPMethod.POST | HTTPMethod.PUT | HTTPMethod.PATCH,
  data: TBody
): Partial<RequestInit> {
  if (data instanceof URLSearchParams) {
    return { method, ...withFormPayload(data) };
  } else {
    return { method, ...withJSONPayload(data) };
  }
}

function withoutPayload(
  method: HTTPMethod.GET | HTTPMethod.DELETE
): Partial<RequestInit> {
  return {
    method,
    headers: {
      accept: CONTENT_TYPE_JSON,
    },
  };
}

const checkStatus = (response: Response): Promise<Response> => {
  if (!response.ok) {
    return Promise.reject(response);
  }
  return Promise.resolve(response);
};

function isGenericApiResponse(
  response: unknown | unknown[]
): response is GenericApiResponse {
  return (
    objectHasProperty(response, "timestamp") &&
    objectHasProperty(response, "data") &&
    objectHasProperty(response, "debugMessage") &&
    objectHasProperty(response, "message") &&
    objectHasProperty(response, "result") &&
    objectHasProperty(response, "status")
  );
}

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
// to provide error messages for the user, we added this
// temp fix. It can be removed once the API is updated
async function checkForFailureHiddenAsSuccess<TResponse extends SomeObject>(
  response: TResponse
): Promise<TResponse> {
  if (isGenericApiResponse(response)) {
    const res: GenericApiResponse = response;
    if (res.result?.toLowerCase().startsWith("failure")) {
      const httpError: HTTPError = {
        data: { message: res.result },
        status: 400,
        statusText: "Bad Request",
        headers: new Headers(),
      };
      return Promise.reject(httpError);
    }
  }
  return Promise.resolve(response);
}

function parseResponseBody<T extends SomeObject>(
  response: Response
): Promise<T> {
  const contentType = response.headers.get("content-type");
  if (contentType === CONTENT_TYPE_JSON) {
    if (response.status === 204) {
      return Promise.resolve({} as unknown as T);
    } else {
      return response.json().then((data) => data as unknown as T);
    }
  } else {
    return response.text().then((data) => data as unknown as T);
  }
}

function handleHTTPError(errorOrResponse: Error | Response): Promise<never> {
  if (errorOrResponse instanceof Response) {
    return parseResponseBody(errorOrResponse).then((body) => {
      const httpError: HTTPError = {
        data: body,
        status: errorOrResponse.status,
        statusText: errorOrResponse.statusText,
        headers: errorOrResponse.headers,
      };
      return Promise.reject(httpError);
    });
  }
  return Promise.reject(errorOrResponse);
}

function handleResponse<TResponse extends SomeObject>(
  response: Response
): Promise<TResponse> {
  return Promise.resolve(response)
    .then(transformHTTPRedirectToLoginTo401)
    .then(transformHTTPRedirectToRootTo204)
    .then(checkStatus)
    .then((response) => parseResponseBody<TResponse>(response))
    .then(checkForFailureHiddenAsSuccess)
    .catch(handleHTTPError);
}

function withPayloadAndVerb<
  TResponse extends SomeObject,
  TBody extends SomeObject | URLSearchParams
>(
  method: HTTPMethod.POST | HTTPMethod.PUT | HTTPMethod.PATCH,
  pathname: AbsolutePathname,
  data: TBody
): Promise<TResponse> {
  return fetch(`${API_BASE_URL}${pathname}`, withPayload(method, data)).then(
    (response) => handleResponse<TResponse>(response)
  );
}

function withoutPayloadandWithVerb<TResponse extends SomeObject>(
  method: HTTPMethod.GET | HTTPMethod.DELETE,
  pathname: AbsolutePathname
): Promise<TResponse> {
  return fetch(`${API_BASE_URL}${pathname}`, withoutPayload(method)).then(
    (response) => handleResponse<TResponse>(response)
  );
}

const get = <T extends SomeObject>(pathname: AbsolutePathname) =>
  withoutPayloadandWithVerb<T>(HTTPMethod.GET, pathname);

const post = <
  TResponse extends SomeObject,
  TBody extends SomeObject | URLSearchParams
>(
  pathname: AbsolutePathname,
  data: TBody
): Promise<TResponse> => withPayloadAndVerb(HTTPMethod.POST, pathname, data);

const put = <TBody extends SomeObject | URLSearchParams>(
  pathname: AbsolutePathname,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PUT, pathname, data);

const patch = <TBody extends SomeObject | URLSearchParams>(
  pathname: AbsolutePathname,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PATCH, pathname, data);

const delete_ = (pathname: AbsolutePathname) =>
  withoutPayloadandWithVerb(HTTPMethod.DELETE, pathname);

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  get,
  post,
  put,
  patch,
  delete: delete_,
};

export type { AbsolutePathname, HTTPError, GenericApiResponse };
export { HTTPMethod, isUnauthorizedError, isServerError, isClientError };
