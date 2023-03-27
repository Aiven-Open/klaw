import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import { transformGetSchemaRequests } from "src/domain/schema-request/schema-request-transformer";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";
import api from "src/services/api";
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
    `/uploadSchema`,
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
      "pageNo" | "env" | "topic"
    >
>;

const getSchemaRequestsForApprover = (
  args: GetSchemaRequestsForApproverQueryParams
): Promise<SchemaRequestApiResponse> => {
  const queryObject: GetSchemaRequestsForApproverQueryParams = {
    pageNo: args.pageNo,
    requestStatus: args.requestStatus,
    ...(args.topic && { topic: args.topic }),
    ...(args.env && args.env !== "ALL" && { env: args.env }),
  };

  return api
    .get<KlawApiResponse<"getSchemaRequestsForApprover">>(
      `/getSchemaRequestsForApprover?${new URLSearchParams(
        queryObject
      ).toString()}`
    )
    .then(transformGetSchemaRequests);
};

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Pick<
    KlawApiRequestQueryParameters<"getSchemaRequests">,
    | "pageNo"
    | "requestStatus"
    | "topic"
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
    ...(args.topic && { topic: args.topic }),
    ...(args.env && args.env !== "ALL" && { env: args.env }),
    ...(args.operationType &&
      args.operationType !== undefined && { env: args.operationType }),
    ...(args.isMyRequest && { isMyRequest: String(Boolean(args.isMyRequest)) }),
  };

  return api
    .get<KlawApiResponse<"getSchemaRequests">>(
      `/getSchemaRequests?${new URLSearchParams(queryObject).toString()}`
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
  >(`/request/approve`, {
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
  >(`/request/decline`, {
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
  >(`/request/delete`, {
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
