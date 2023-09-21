import isArray from "lodash/isArray";
import isPlainObject from "lodash/isPlainObject";
import { getHTTPBaseAPIUrl } from "src/config";
import { objectHasProperty } from "src/services/type-utils";
import {
  operations as ApiOperations,
  paths as ApiPaths,
  components,
} from "types/api";
import { ResolveIntersectionTypes } from "types/utils";

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

const CONTENT_TYPE_JSON = "application/json" as const;

const API_BASE_URL = getHTTPBaseAPIUrl();

const API_PATHS = {
  approveOperationalRequest: "/operationalRequest/reqId/{reqId}/approve",
  declineOperationalRequest: "/operationalRequest/reqId/{reqId}/decline",
  deleteOperationalRequest: "/operationalRequest/reqId/{reqId}/delete",
  getOperationalRequests: "/operationalRequests/requestsFor/{requestsFor}",
  validateOffsetRequestDetails:
    "/operationalRequest/consumerOffsetsReset/validate",
  createConsumerOffsetsResetRequest:
    "/operationalRequest/consumerOffsetsReset/create",
  restartConnector: "/connector/restart",
  getConnectorsToManage: "/getConnectorsToManage",
  resetCacheClusterApi: "/schemas/resetCache",
  updateSyncSchemas: "/schemas",
  getSchemasOfEnvironment: "/schemas",
  validateSchema: "/validate/schema",
  updateUserTeamFromSwitchTeams: "/user/updateTeam",
  uploadSchema: "/uploadSchema",
  updateUser: "/updateUser",
  createTopicsUpdateRequest: "/updateTopics",
  updateTeam: "/updateTeam",
  updateSyncTopics: "/updateSyncTopics",
  updateSyncTopicsBulk: "/updateSyncTopicsBulk",
  updateSyncConnectors: "/updateSyncConnectors",
  updateSyncBackTopics: "/updateSyncBackTopics",
  updateSyncBackAcls: "/updateSyncBackAcls",
  updateSyncAcls: "/updateSyncAcls",
  updateProfile: "/updateProfile",
  updatePermissions: "/updatePermissions",
  updateKwCustomProperty: "/updateKwCustomProperty",
  udpateTenant: "/udpateTenant",
  udpateTenantExtension: "/udpateTenantExtension",
  sendMessageToAdmin: "/sendMessageToAdmin",
  saveTopicDocumentation: "/saveTopicDocumentation",
  saveConnectorDocumentation: "/saveConnectorDocumentation",
  resetToken: "/reset/token",
  resetPasswordWithToken: "/reset/password",
  deleteRequest: "/request/delete",
  declineRequest: "/request/decline",
  approveRequest: "/request/approve",
  registerUser: "/registerUser",
  registerUserSaas: "/registerUserSaas",
  promoteSchema: "/promote/schema",
  logout: "/logout",
  approveTopicRequests: "/execTopicRequests",
  declineTopicRequests: "/execTopicRequestsDecline",
  execSchemaRequests: "/execSchemaRequests",
  execSchemaRequestsDecline: "/execSchemaRequestsDecline",
  declineNewUserRequests: "/execNewUserRequestDecline",
  approveNewUserRequests: "/execNewUserRequestApprove",
  approveTopicRequests_1: "/execConnectorRequests",
  declineConnectorRequests: "/execConnectorRequestsDecline",
  approveAclRequests: "/execAclRequest",
  declineAclRequests: "/execAclRequestDecline",
  deleteUser: "/deleteUserRequest",
  deleteTopicRequests: "/deleteTopicRequests",
  deleteTenant: "/deleteTenant",
  deleteTeam: "/deleteTeamRequest",
  deleteSchemaRequests: "/deleteSchemaRequests",
  deleteRole: "/deleteRole",
  deleteEnvironment: "/deleteEnvironmentRequest",
  deleteConnectorRequests: "/deleteConnectorRequests",
  deleteCluster: "/deleteCluster",
  deleteAclRequests: "/deleteAclRequests",
  createTopicsCreateRequest: "/createTopics",
  createTopicDeleteRequest: "/createTopicDeleteRequest",
  deleteAclSubscriptionRequest: "/createDeleteAclSubscriptionRequest",
  createConnectorRequest: "/createConnector",
  createConnectorDeleteRequest: "/createConnectorDeleteRequest",
  createClaimTopicRequest: "/createClaimTopicRequest",
  createClaimConnectorRequest: "/createClaimConnectorRequest",
  createAcl: "/createAcl",
  changePwd: "/chPwd",
  addTenantId: "/addTenantId",
  addRoleId: "/addRoleId",
  addNewUser: "/addNewUser",
  addNewTeam: "/addNewTeam",
  addNewEnv: "/addNewEnv",
  addNewCluster: "/addNewCluster",
  testClusterApiConnection: "/testClusterApiConnection",
  shutdownApp: "/shutdownContext",
  showUsers: "/showUserList",
  resetMemoryCache: "/resetMemoryCache",
  resetCache: "/resetCache",
  getRequestStatistics: "/requests/statistics",
  getRegistrationInfoFromId: "/getUserInfoFromRegistrationId",
  getUserDetails: "/getUserDetails",
  getUpdateEnvStatus: "/getUpdateEnvStatus",
  getTopics: "/getTopics",
  getTopicsRowView: "/getTopicsRowView",
  getTopicsOnly: "/getTopicsOnly",
  getTopicsCountPerEnv: "/getTopicsCountPerEnv",
  getTopicTeam: "/getTopicTeam",
  getTopicRequests: "/getTopicRequests",
  getTopicRequestsForApprover: "/getTopicRequestsForApprover",
  getTopicEvents: "/getTopicEvents",
  getTopicDetailsPerEnv: "/getTopicDetailsPerEnv",
  getTenants: "/getTenants",
  getTenantsInfo: "/getTenantsInfo",
  getTeamsOverview: "/getTeamsOverview",
  getTeamDetails: "/getTeamDetails",
  getSyncTopics: "/getSyncTopics",
  getSyncEnv: "/getSyncEnv",
  getSyncConnectors: "/getSyncConnectors",
  getSyncConnectorsEnv: "/getSyncConnectorsEnv",
  getSyncBackAcls: "/getSyncBackAcls",
  getSyncAcls: "/getSyncAcls",
  getStandardEnvNames: "/getStandardEnvNames",
  getSchemaRequests: "/getSchemaRequests",
  getSchemaRequestsForApprover: "/getSchemaRequestsForApprover",
  getSchemaRegEnvs: "/getSchemaRegEnvs",
  getSchemaOfTopic: "/getSchemaOfTopic",
  getRoles: "/getRoles",
  getRolesFromDb: "/getRolesFromDb",
  getRequestTypeStatuses: "/getRequestTypeStatuses",
  getPermissions: "/getPermissions",
  getPermissionDescriptions: "/getPermissionDescriptions",
  getNewUserRequests: "/getNewUserRequests",
  getMyTenantInfo: "/getMyTenantInfo",
  getMyProfileInfo: "/getMyProfileInfo",
  getKwReport: "/getKwReport",
  getKwPubkey: "/getKwPubkey",
  getSupportedKafkaProtocols: "/getKafkaProtocols",
  getKafkaConnectEnvs: "/getKafkaConnectEnvs",
  getExtensionPeriods: "/getExtensionPeriods",
  getEnvs: "/getEnvs",
  getEnvsPaginated: "/getEnvsPaginated",
  getEnvsForSchemaRequests: "/getEnvsForSchemaRequests",
  getEnvsBaseCluster: "/getEnvsBaseCluster",
  getEnvsBaseClusterFilteredForTeam: "/getEnvsBaseClusterFilteredForTeam",
  getSchemaRegEnvsPaginated: "/environments/schemaRegistry",
  getKafkaEnvsPaginated: "/environments/kafka",
  getKafkaConnectEnvsPaginated: "/environments/kafkaconnect",
  getEnvParams: "/getEnvParams",
  getEnvDetails: "/getEnvDetails",
  getDbAuth: "/getDbAuth",
  getDashboardStats: "/getDashboardStats",
  getConsumerOffsets: "/getConsumerOffsets",
  getConnectors: "/getConnectors",
  getConnectorRequests: "/getConnectorRequests",
  getCreatedConnectorRequests: "/getConnectorRequestsForApprover",
  getConnectorOverview: "/getConnectorOverview",
  getConnectorDetails: "/getConnectorDetails",
  getConnectorDetailsPerEnv: "/getConnectorDetailsPerEnv",
  getClusters: "/getClusters",
  getClustersPaginated: "/getClustersPaginated",
  getClusterInfoFromEnv: "/getClusterInfoFromEnv",
  getClusterDetails: "/getClusterDetails",
  getBrokerTopMetrics: "/getBrokerTopMetrics",
  getBasicInfo: "/getBasicInfo",
  getAuth: "/getAuth",
  getAllTeamsSU: "/getAllTeamsSU",
  getAllTeamsSUOnly: "/getAllTeamsSUOnly",
  getAllTeamsSUFromRegisterUsers: "/getAllTeamsSUFromRegisterUsers",
  getAllEditableProps: "/getAllServerEditableConfig",
  getAllProperties: "/getAllServerConfig",
  getAivenServiceAccounts: "/getAivenServiceAccounts",
  getAivenServiceAccountDetails: "/getAivenServiceAccount",
  getAdvancedTopicConfigs: "/getAdvancedTopicConfigs",
  showActivityLog: "/getActivityLogPerEnv",
  getActivityLogForTeamOverview: "/getActivityLogForTeamOverview",
  getActivationInfo: "/getActivationInfo",
  getTopicOverview: "/getTopicOverview",
  getAclsCountPerEnv: "/getAclsCountPerEnv",
  getAclRequests: "/getAclRequests",
  getAclRequestsForApprover: "/getAclRequestsForApprover",
  getAclCommand: "/getAclCommands",
} satisfies {
  [key in keyof Omit<
    ApiOperations,
    | "getSchemaOfTopicFromSource"
    | "getSwitchTeams"
    | "getTopicRequest"
    | "getKafkaEnv"
    | "getKafkaConnectEnv"
    | "getSchemaRegEnv"
    | "addEnvToCache"
    | "removeEnvFromCache"
  >]: keyof ApiPaths;
};

type GetSchemaOfTopicFromSource = (params: {
  source: string;
  kafkaEnvId: string;
  topicName: string;
  schemaVersion: string;
}) => keyof ApiPaths;
type GetSwitchTeams = (params: { userId: string }) => keyof ApiPaths;
type GetTopicRequest = (params: { topicReqId: string }) => keyof ApiPaths;
type GetKafkaEnv = (params: { envId: string }) => keyof ApiPaths;
type GetConnectEnv = (params: { envId: string }) => keyof ApiPaths;
type GetSchemaRegEnv = (params: { envId: string }) => keyof ApiPaths;
type AddEnvToCache = (params: {
  tenantId: string;
  id: string;
}) => keyof ApiPaths;
type RemoveEnvFromCache = (params: {
  tenantId: string;
  id: string;
}) => keyof ApiPaths;

const DYNAMIC_API_PATHS = {
  getSchemaOfTopicFromSource: ({
    source,
    kafkaEnvId,
    topicName,
    schemaVersion,
  }: Parameters<GetSchemaOfTopicFromSource>[0]) =>
    `/schemas/source/${source}/kafkaEnv/${kafkaEnvId}/topic/${topicName}/schemaVersion/${schemaVersion}` as keyof ApiPaths,
  getSwitchTeams: ({ userId }: Parameters<GetSwitchTeams>[0]) =>
    `/user/${userId}/switchTeamsList` as keyof ApiPaths,
  getTopicRequest: ({ topicReqId }: Parameters<GetTopicRequest>[0]) =>
    `/topic/request/${topicReqId}` as keyof ApiPaths,
  getKafkaEnv: ({ envId }: Parameters<GetKafkaEnv>[0]) =>
    `/environments/kafka/${envId}` as keyof ApiPaths,
  getKafkaConnectEnv: ({ envId }: Parameters<GetConnectEnv>[0]) =>
    `/environments/kafkaconnect/${envId}` as keyof ApiPaths,
  getSchemaRegEnv: ({ envId }: Parameters<GetSchemaRegEnv>[0]) =>
    `/environments/schemaRegistry/${envId}` as keyof ApiPaths,
  addEnvToCache: ({ tenantId, id }: Parameters<AddEnvToCache>[0]) =>
    `/cache/tenant/${tenantId}/entityType/environment/id/${id}` as keyof ApiPaths,
  removeEnvFromCache: ({ tenantId, id }: Parameters<RemoveEnvFromCache>[0]) =>
    `/cache/tenant/${tenantId}/entityType/environment/id/${id}` as keyof ApiPaths,
} satisfies {
  [key in keyof Pick<
    ApiOperations,
    | "getSchemaOfTopicFromSource"
    | "getSwitchTeams"
    | "getTopicRequest"
    | "getKafkaEnv"
    | "getKafkaConnectEnv"
    | "getSchemaRegEnv"
    | "addEnvToCache"
    | "removeEnvFromCache"
  >]:
    | GetSchemaOfTopicFromSource
    | GetSwitchTeams
    | GetTopicRequest
    | GetKafkaEnv
    | GetConnectEnv
    | GetSchemaRegEnv
    | AddEnvToCache
    | RemoveEnvFromCache;
};

type Params = URLSearchParams;

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
  // errorOrResponse is a Response when the api response
  // was identified as an error `checkStatus` and we've
  // not yet read the body stream
  if (errorOrResponse instanceof Response) {
    return parseResponseBody(errorOrResponse).then((body) => {
      // We have api endpoints that return ApiResponse[]
      // these endpoints are all meant for enabling "batch" processing,
      // for example to delete multiple requests
      // this is currently not implemented as a feature.
      // If these endpoints contain an error, it will be contained
      // in the first (and only) entry of the ApiResponse[]
      // see more details: https://github.com/aiven/klaw/pull/921#issue-1641959704
      const bodyToCheck = isArray(body) ? body[0] : body;
      if (isKlawApiError(bodyToCheck)) {
        const error: KlawApiError = bodyToCheck;
        return Promise.reject(error);
      }

      const httpError: HTTPError = {
        // bodyToCheck is unknown here, so we need to coerce its type to avoid TS errors
        data: bodyToCheck as string | SomeObject,
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
  TBody extends SomeObject | URLSearchParams,
>(
  method: HTTPMethod.POST | HTTPMethod.PUT | HTTPMethod.PATCH,
  pathname: keyof ApiPaths,
  data: TBody
): Promise<TResponse> {
  return fetch(`${API_BASE_URL}${pathname}`, withPayload(method, data)).then(
    (response) => handleResponse<TResponse>(response)
  );
}

function withoutPayloadAndWithVerb<TResponse extends SomeObject>(
  method: HTTPMethod.GET | HTTPMethod.DELETE | HTTPMethod.POST,
  pathname: keyof ApiPaths,
  params?: Params
): Promise<TResponse> {
  return fetch(
    `${API_BASE_URL}${pathname}${!params ? "" : `?${params}`}`,
    withoutPayload(method)
  ).then((response) => handleResponse<TResponse>(response));
}

const get = <T extends SomeObject>(pathname: keyof ApiPaths, params?: Params) =>
  withoutPayloadAndWithVerb<T>(HTTPMethod.GET, pathname, params);

const post = <
  TResponse extends SomeObject,
  TBody extends SomeObject | URLSearchParams | never,
>(
  pathname: keyof ApiPaths,
  data?: TBody
): Promise<TResponse> => {
  if (data === undefined) {
    return withoutPayloadAndWithVerb(HTTPMethod.POST, pathname);
  }
  return withPayloadAndVerb(HTTPMethod.POST, pathname, data);
};

const put = <TBody extends SomeObject | URLSearchParams>(
  pathname: keyof ApiPaths,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PUT, pathname, data);

const patch = <TBody extends SomeObject | URLSearchParams>(
  pathname: keyof ApiPaths,
  data: TBody
) => withPayloadAndVerb(HTTPMethod.PATCH, pathname, data);

const delete_ = (pathname: keyof ApiPaths) =>
  withoutPayloadAndWithVerb(HTTPMethod.DELETE, pathname);

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  get,
  post,
  put,
  patch,
  delete: delete_,
};

export type { HTTPError, KlawApiResponse, KlawApiError };
export {
  API_PATHS,
  DYNAMIC_API_PATHS,
  HTTPMethod,
  isUnauthorizedError,
  isServerError,
  isClientError,
  isKlawApiError,
};
