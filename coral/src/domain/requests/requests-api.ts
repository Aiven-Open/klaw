import { getRequestsWaitingForApprovalTransformer } from "src/domain/requests/requests-transformers";
import api, { API_PATHS } from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getRequestsStatistics = (
  params: KlawApiRequestQueryParameters<"getRequestStatistics">
): Promise<KlawApiResponse<"getRequestStatistics">> => {
  return api.get<KlawApiResponse<"getRequestStatistics">>(
    API_PATHS.getRequestStatistics,
    new URLSearchParams(params)
  );
};

const getRequestsWaitingForApproval = () => {
  return getRequestsStatistics({ requestMode: "MY_APPROVALS" }).then(
    getRequestsWaitingForApprovalTransformer
  );
};

export { getRequestsStatistics, getRequestsWaitingForApproval };
