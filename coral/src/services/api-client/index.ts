import { getHTTPBaseAPIUrl } from "src/config";

export enum HTTPMethod {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
  PATCH = "PATCH",
  DELETE = "DELETE",
}
type SomeObject = Record<string, unknown> | Record<string, never>;
export type AbsolutePathname = `/${string}`;
const CONTENT_TYPE_JSON = "application/json" as const;

const API_BASE_URL = getHTTPBaseAPIUrl();

class HTTPError extends Error {
  status: number;
  statusText: string;
  constructor(
    status: number,
    statusText: string,
    message?: string,
    options?: ErrorOptions
  ) {
    super(message, options);
    this.status = status;
    this.statusText = statusText;
  }
}

export class ServerError extends HTTPError {}

export class ClientError extends HTTPError {}

export class UnauthorizedError extends ClientError {
  constructor(message?: string, options?: ErrorOptions) {
    super(401, "Unauthorized", message, options);
  }
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
    throw new UnauthorizedError();
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
    return { method, redirect: "manual", ...withFormPayload(data) };
  } else {
    return { method, redirect: "manual", ...withJSONPayload(data) };
  }
}

function withoutPayload(
  method: HTTPMethod.GET | HTTPMethod.DELETE
): Partial<RequestInit> {
  return {
    method,
    redirect: "manual",
    headers: {
      accept: CONTENT_TYPE_JSON,
    },
  };
}

const checkStatus = (response: Response): Response => {
  if (!response.ok) {
    if (response.status === 401) {
      throw new UnauthorizedError();
    } else if (response.status >= 500) {
      throw new ServerError(response.status, response.statusText);
    } else if (response.status >= 400) {
      throw new ClientError(response.status, response.statusText);
    }
  }
  return response;
};

function parseResponseBody<T extends SomeObject>(response: Response): T {
  const contentType = response.headers.get("content-type");
  if (contentType === CONTENT_TYPE_JSON) {
    if (response.status === 204) {
      return {} as unknown as T;
    } else {
      return response.json() as unknown as T;
    }
  } else {
    return response.text() as unknown as T;
  }
}

function handleHTTPError(error: Error): never {
  throw error;
}

function handleResponse<TResponse extends SomeObject>(
  response: Response
): Promise<TResponse> {
  return Promise.resolve(response)
    .then(transformHTTPRedirectToLoginTo401)
    .then(transformHTTPRedirectToRootTo204)
    .then(checkStatus)
    .then((response) => parseResponseBody<TResponse>(response))
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

const get = (pathname: AbsolutePathname) =>
  withoutPayloadandWithVerb(HTTPMethod.GET, pathname);

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

export default {
  get,
  post,
  put,
  patch,
  delete: delete_,
};
