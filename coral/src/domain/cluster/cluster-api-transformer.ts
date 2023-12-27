import { KlawApiModel } from "types/utils";
import { ClustersPaginatedApiResponse } from "src/domain/cluster/cluster-types";

function transformPaginatedClustersApiResponse(
  apiResponse: KlawApiModel<"KwClustersModelResponse">[]
): ClustersPaginatedApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    // Backend fix to add currentPage is open right now
    // PR #2071
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    currentPage: Number(apiResponse[0].currentPage || 1),
    entries: apiResponse,
  };
}

export { transformPaginatedClustersApiResponse };
