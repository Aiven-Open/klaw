import { KlawApiResponse, ResolveIntersectionTypes } from "types/utils";
import api from "src/services/api";
import {
  SchemaRequestApiResponse,
  CreateSchemaRequestPayload,
  SchemaRequestStatus,
} from "src/domain/schema-request/schema-request-types";
import { components, operations } from "types/api";
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

// query: {
//   pageNo: string;
//   currentPage?: string;
//   requestStatus?: components["schemas"]["RequestStatus"];
//   /** Name of a topic */
//   topic?: string;
//   /** Environment identifier */
//   env?: string;
//   search?: string;
// };
const getSchemaRequestsForApprover = ({
  requestStatus = "CREATED",
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

export { createSchemaRequest, getSchemaRequestsForApprover };
