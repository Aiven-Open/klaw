import { getHTTPBaseAPIUrl } from "src/config";
import isPlainObject from "lodash/isPlainObject";
import { components } from "types/api";
import { objectHasProperty } from "src/services/type-utils";
import { ResolveIntersectionTypes } from "types/utils";
import isArray from "lodash/isArray";

type KlawApiResponse = ResolveIntersectionTypes<
  components["schemas"]["ApiResponse"]
>;
type KlawApiError = KlawApiResponse & {
  success: false;
};

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

type UnauthorizedError = ResolveIntersectionTypes<HTTPError & { status: 401 }>;
type ClientError = ResolveIntersectionTypes<
  HTTPError & {
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
  }
>;

type ServerError = ResolveIntersectionTypes<
  HTTPError & {
    status:
      | 500
      | 501
      | 502
      | 503
      | 504
      | 505
      | 506
      | 507
      | 508
      | 510
      | 511
      | 599;
  }
>;

function isHTTPErrorProperties(
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
  if (isRecord(value) && isHTTPErrorProperties(value)) {
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
  method: HTTPMethod.GET | HTTPMethod.DELETE | HTTPMethod.POST
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

function isKlawApiError(
  response: unknown | unknown[]
): response is KlawApiError {
  return (
    objectHasProperty(response, "message") &&
    objectHasProperty(response, "success") &&
    response.success === false
  );
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

function checkForKlawErrors<TResponse extends SomeObject>(
  parsedResponse: TResponse
): Promise<TResponse> {
  if (isKlawApiError(parsedResponse)) {
    return Promise.reject(parsedResponse);
  }
  return Promise.resolve(parsedResponse);
}

function handleError(
  errorOrResponse: Error | Response | KlawApiError
): Promise<never> {
  // errorOrResponse is an Response when the api response
  // was identified as an error `checkStatus` and we've
  // not yet read the body stream
  if (errorOrResponse instanceof Response) {
    return parseResponseBody(errorOrResponse).then((body) => {
      // We have api endpoints that return ApiResponse[]
      // these endpoints are all meant for enabling "batch" processing,
      // for example to delete multiple requests
      // this is currently not implemented as a feature.
      // If this endpoints contain an error, it will be contained
      // in the first (and only) entry of the ApiResponse[]
      // see more details: https://github.com/aiven/klaw/pull/921#issue-1641959704
      const bodyToCheck = isArray(body) ? body[0] : body;
      if (isKlawApiError(bodyToCheck)) {
        const error: KlawApiError = bodyToCheck;
        return Promise.reject(error);
      }

      const httpError: HTTPError = {
        data: bodyToCheck,
        status: errorOrResponse.status,
        statusText: errorOrResponse.statusText,
        headers: errorOrResponse.headers,
      };
      return Promise.reject(httpError);
    });
  }
  return Promise.reject(errorOrResponse as Error);
}

function handleResponse<TResponse extends SomeObject>(
  response: Response
): Promise<TResponse> {
  return Promise.resolve(response)
    .then(transformHTTPRedirectToLoginTo401)
    .then(transformHTTPRedirectToRootTo204)
    .then(checkStatus)
    .then((response) => parseResponseBody<TResponse>(response))
    .then(checkForKlawErrors)
    .catch(handleError);
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

function withoutPayloadAndWithVerb<TResponse extends SomeObject>(
  method: HTTPMethod.GET | HTTPMethod.DELETE | HTTPMethod.POST,
  pathname: AbsolutePathname
): Promise<TResponse> {
  return fetch(`${API_BASE_URL}${pathname}`, withoutPayload(method)).then(
    (response) => handleResponse<TResponse>(response)
  );
}

const get = <T extends SomeObject>(pathname: AbsolutePathname) =>
  withoutPayloadAndWithVerb<T>(HTTPMethod.GET, pathname);

const post = <
  TResponse extends SomeObject,
  TBody extends SomeObject | URLSearchParams | never
>(
  pathname: AbsolutePathname,
  data?: TBody
): Promise<TResponse> => {
  if (data === undefined) {
    return withoutPayloadAndWithVerb(HTTPMethod.POST, pathname);
  }
  return withPayloadAndVerb(HTTPMethod.POST, pathname, data);
};

const put = <TBody extends SomeObject | URLSearchParams>(
  pathname: AbsolutePathname,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PUT, pathname, data);

const patch = <TBody extends SomeObject | URLSearchParams>(
  pathname: AbsolutePathname,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PATCH, pathname, data);

const delete_ = (pathname: AbsolutePathname) =>
  withoutPayloadAndWithVerb(HTTPMethod.DELETE, pathname);

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  get,
  post,
  put,
  patch,
  delete: delete_,
};

export type { AbsolutePathname, HTTPError, KlawApiResponse, KlawApiError };
export {
  HTTPMethod,
  isUnauthorizedError,
  isServerError,
  isClientError,
  isKlawApiError,
};
