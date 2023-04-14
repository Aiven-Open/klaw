import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import { transformGetSchemaRequests } from "src/domain/schema-request/schema-request-transformer";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";
import api, { API_PATHS } from "src/services/api";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
  ResolveIntersectionTypes,
} from "types/utils";

type CreateSchemaRequestPayload = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiRequest<"uploadSchema">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Partial<KlawApiRequest<"uploadSchema">>
>;

const createSchemaRequest = (
  params: CreateSchemaRequestPayload
): Promise<KlawApiResponse<"uploadSchema">> => {
  const payload = {
    ...params,
    schemaversion: "1.0",
    appname: "App",
  };

  return api.post<KlawApiResponse<"uploadSchema">, CreateSchemaRequestPayload>(
    API_PATHS.uploadSchema,
    payload
  );
};

type GetSchemaRequestsForApproverQueryParams = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiRequestQueryParameters<"getSchemaRequestsForApprover">,
      "requestStatus"
    >
  > &
    Pick<
      KlawApiRequestQueryParameters<"getSchemaRequestsForApprover">,
      "pageNo" | "env" | "search" | "operationType"
    >
>;

const getSchemaRequestsForApprover = (
  args: GetSchemaRequestsForApproverQueryParams
): Promise<SchemaRequestApiResponse> => {
  const queryObject: GetSchemaRequestsForApproverQueryParams = {
    pageNo: args.pageNo,
    requestStatus: args.requestStatus,
    ...(args.search && args.search !== "" && { search: args.search }),
    ...(args.env && args.env !== "ALL" && { env: args.env }),
    ...(args.operationType !== undefined && {
      operationType: args.operationType,
    }),
  };

  return api
    .get<KlawApiResponse<"getSchemaRequestsForApprover">>(
      API_PATHS.getSchemaRequestsForApprover,
      new URLSearchParams(queryObject)
    )
    .then(transformGetSchemaRequests);
};

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Pick<
    KlawApiRequestQueryParameters<"getSchemaRequests">,
    | "pageNo"
    | "requestStatus"
    | "search"
    | "env"
    | "isMyRequest"
    | "operationType"
  >
>;

const getSchemaRequests = (
  args: GetSchemaRequestsQueryParams
): Promise<SchemaRequestApiResponse> => {
  const queryObject: Omit<GetSchemaRequestsQueryParams, "isMyRequest"> & {
    myRequest?: "true" | "false";
  } = {
    pageNo: args.pageNo,
    ...(args.operationType && { operationType: args.operationType }),
    ...(args.requestStatus && { requestStatus: args.requestStatus }),
    ...(args.search && args.search !== "" && { search: args.search }),
    ...(args.env && args.env !== "ALL" && { env: args.env }),
    ...(args.operationType !== undefined && { env: args.operationType }),
    ...(args.isMyRequest && { isMyRequest: String(Boolean(args.isMyRequest)) }),
  };

  return api
    .get<KlawApiResponse<"getSchemaRequests">>(
      API_PATHS.getSchemaRequests,
      new URLSearchParams(queryObject)
    )
    .then(transformGetSchemaRequests);
};

const approveSchemaRequest = ({
  reqIds,
}: {
  reqIds: RequestVerdictApproval<"SCHEMA">["reqIds"];
}) => {
  return api.post<
    KlawApiResponse<"approveRequest">,
    RequestVerdictApproval<"SCHEMA">
  >(API_PATHS.approveRequest, {
    reqIds,
    requestEntityType: "SCHEMA",
  });
};

const declineSchemaRequest = ({
  reqIds,
  reason,
}: Omit<RequestVerdictDecline<"SCHEMA">, "requestEntityType">) => {
  return api.post<
    KlawApiResponse<"declineRequest">,
    RequestVerdictDecline<"SCHEMA">
  >(API_PATHS.declineRequest, {
    reqIds,
    reason,
    requestEntityType: "SCHEMA",
  });
};

const deleteSchemaRequest = ({
  reqIds,
}: Omit<RequestVerdictDelete<"SCHEMA">, "requestEntityType">) => {
  return api.post<
    KlawApiResponse<"deleteRequest">,
    RequestVerdictDelete<"SCHEMA">
  >(API_PATHS.deleteRequest, {
    reqIds,
    requestEntityType: "SCHEMA",
  });
};

export {
  createSchemaRequest,
  getSchemaRequestsForApprover,
  approveSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
  deleteSchemaRequest,
};
