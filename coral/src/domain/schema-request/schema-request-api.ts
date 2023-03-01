import { KlawApiResponse, ResolveIntersectionTypes } from "types/utils";
import api from "src/services/api";
import {
  SchemaRequestApiResponse,
  CreateSchemaRequestPayload,
} from "src/domain/schema-request/schema-request-types";
import { operations } from "types/api";
import { transformGetSchemaRequestsForApproverResponse } from "src/domain/schema-request/schema-request-transformer";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
} from "src/domain/requests";

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

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Required<
    Pick<
      operations["getSchemaRequestsForApprover"]["parameters"]["query"],
      "pageNo" | "requestStatus"
    >
  > &
    Pick<
      operations["getSchemaRequestsForApprover"]["parameters"]["query"],
      "env" | "topic"
    >
>;

const getSchemaRequestsForApprover = (
  args: GetSchemaRequestsQueryParams
): Promise<SchemaRequestApiResponse> => {
  const queryObject: GetSchemaRequestsQueryParams = {
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
    .then(transformGetSchemaRequestsForApproverResponse);
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
  >(`/request/approve`, {
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
};
