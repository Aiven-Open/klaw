import { KlawApiResponse, ResolveIntersectionTypes } from "types/utils";
import api from "src/services/api";
import {
  SchemaRequestApiResponse,
  CreateSchemaRequestPayload,
} from "src/domain/schema-request/schema-request-types";
import { operations } from "types/api";
import { transformGetSchemaRequestsForApproverResponse } from "src/domain/schema-request/schema-request-transformer";

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
  console.log("api call", args);
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

export { createSchemaRequest, getSchemaRequestsForApprover };
