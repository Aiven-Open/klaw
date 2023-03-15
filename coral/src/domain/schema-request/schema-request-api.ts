import {
  KlawApiModel,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
  ResolveIntersectionTypes,
} from "types/utils";
import api from "src/services/api";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";
import { transformGetSchemaRequests } from "src/domain/schema-request/schema-request-transformer";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
} from "src/domain/requests/requests-types";

type CreateSchemaRequestPayload = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Partial<KlawApiModel<"SchemaRequest">>
>;

const createSchemaRequest = (
  params: CreateSchemaRequestPayload
): Promise<KlawApiResponse<"schemaUpload">> => {
  const payload = {
    ...params,
    schemaversion: "1.0",
    appname: "App",
  };

  return api.post<KlawApiResponse<"schemaUpload">, CreateSchemaRequestPayload>(
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
    "pageNo" | "requestStatus" | "topic" | "env" | "isMyRequest"
  >
>;

const getSchemaRequests = (
  args: GetSchemaRequestsQueryParams
): Promise<SchemaRequestApiResponse> => {
  const queryObject: Omit<GetSchemaRequestsQueryParams, "isMyRequest"> & {
    myRequest?: "true" | "false";
  } = {
    pageNo: args.pageNo,
    ...(args.requestStatus && { requestStatus: args.requestStatus }),
    ...(args.topic && { topic: args.topic }),
    ...(args.env && args.env !== "ALL" && { env: args.env }),
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

export {
  createSchemaRequest,
  getSchemaRequestsForApprover,
  approveSchemaRequest,
  declineSchemaRequest,
  getSchemaRequests,
};
