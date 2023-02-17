import {
  KlawApiRequest,
  KlawApiResponse,
  ResolveIntersectionTypes,
} from "types/utils";
import api from "src/services/api";
import {
  SchemaRequestApiResponse,
  SchemaRequestPayload,
  SchemaRequestStatus,
} from "src/domain/schema-request/schema-request-types";
import { operations } from "types/api";
import { transformGetSchemaRequestsForApproverResponse } from "src/domain/schema-request/schema-request-transformer";

const createSchemaRequest = (
  params: SchemaRequestPayload
): Promise<KlawApiResponse<"schemaUpload">> => {
  const payload = {
    ...params,
    schemaversion: "1.0",
    appname: "App",
  };

  return api.post<
    KlawApiResponse<"schemaUpload">,
    KlawApiRequest<"schemaUpload">
  >(`/uploadSchema`, payload);
};

type GetSchemaRequestsArgs = {
  pageNumber?: number;
  requestStatus: SchemaRequestStatus;
};

type GetSchemaRequestsQueryParams = ResolveIntersectionTypes<
  Required<
    Pick<
      operations["getSchemaRequestsForApprover"]["parameters"]["query"],
      "pageNo" | "requestsType"
    >
  >
>;

const getSchemaRequestsForApprover = ({
  requestStatus,
  pageNumber = 1,
}: GetSchemaRequestsArgs): Promise<SchemaRequestApiResponse> => {
  const queryObject: GetSchemaRequestsQueryParams = {
    pageNo: pageNumber.toString(),
    // This is a naming mix up in backend, we want to query
    // for the status, not the type. Will be changed there soon.
    requestsType: requestStatus,
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
