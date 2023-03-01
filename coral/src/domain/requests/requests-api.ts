import { getRequestsWaitingForApprovalTransformer } from "src/domain/requests/requests-transformers";
import api from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getRequestsStatistics = (
  params: KlawApiRequestQueryParameters<"getRequestStatistics">
): Promise<KlawApiResponse<"getRequestStatistics">> => {
  return api.get<KlawApiResponse<"getRequestStatistics">>(
    `/requests/statistics?${new URLSearchParams(params)}`
  );
};

const getRequestsWaitingForApproval = () => {
  return getRequestsStatistics({ requestMode: "MY_APPROVALS" }).then(
    getRequestsWaitingForApprovalTransformer
  );
};

export { getRequestsStatistics, getRequestsWaitingForApproval };
