import { KlawApiResponse } from "types/utils";
import { SchemaRequestApiResponse } from "src/domain/schema-request/schema-request-types";

function transformGetSchemaRequests(
  apiResponse:
    | KlawApiResponse<"getSchemaRequestsForApprover">
    | KlawApiResponse<"getSchemaRequests">
): SchemaRequestApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage: Number(apiResponse[0].currentPage),
    entries: apiResponse,
  };
}

export { transformGetSchemaRequests };
