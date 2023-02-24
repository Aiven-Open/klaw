import { KlawApiResponse, ResolveIntersectionTypes } from "types/utils";
import api from "src/services/api";
import {
  SchemaRequestApiResponse,
  CreateSchemaRequestPayload,
  SchemaRequestStatus,
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

type GetSchemaRequestsArgs = {
  pageNumber?: number;
  requestStatus: SchemaRequestStatus;
};

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Required<
    Pick<
      operations["getSchemaRequestsForApprover"]["parameters"]["query"],
      "pageNo" | "requestStatus"
    >
  >
>;

const getSchemaRequestsForApprover = ({
  requestStatus,
  pageNumber = 1,
}: GetSchemaRequestsArgs): Promise<SchemaRequestApiResponse> => {
  const queryObject: GetSchemaRequestsQueryParams = {
    pageNo: pageNumber.toString(),
    requestStatus: requestStatus,
  };
  return api
    .get<KlawApiResponse<"getSchemaRequestsForApprover">>(
      `/getSchemaRequestsForApprover?${new URLSearchParams(
        queryObject
      ).toString()}`
    )
    .then(transformGetSchemaRequestsForApproverResponse);
};

const approveSchemaRequest = (payload: RequestVerdictApproval<"SCHEMA">) => {
  return api.post<
    KlawApiResponse<"approveRequest">,
    RequestVerdictApproval<"SCHEMA">
  >(`/request/approve`, payload);
};

const declineSchemaRequest = (payload: RequestVerdictDecline<"SCHEMA">) => {
  return api.post<
    KlawApiResponse<"declineRequest">,
    RequestVerdictDecline<"SCHEMA">
  >(`/request/approve`, payload);
};

export {
  createSchemaRequest,
  getSchemaRequestsForApprover,
  approveSchemaRequest,
  declineSchemaRequest,
};
