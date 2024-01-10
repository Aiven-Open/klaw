import {
  activityLogTransformer,
  getRequestsWaitingForApprovalTransformer,
} from "src/domain/requests/requests-transformers";
import api, { API_PATHS } from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getRequestsStatistics = async (
  params: KlawApiRequestQueryParameters<"getRequestStatistics">
): Promise<KlawApiResponse<"getRequestStatistics">> => {
  const response = await api.get<KlawApiResponse<"getRequestStatistics">>(
    API_PATHS.getRequestStatistics,
    new URLSearchParams(params)
  );

  return response;
};

const getRequestsWaitingForApproval = async () => {
  const response = await getRequestsStatistics({
    requestMode: "MY_APPROVALS",
  }).then(getRequestsWaitingForApprovalTransformer);

  return response;
};

const getActivityLog = async (
  params: KlawApiRequestQueryParameters<"showActivityLog">
) => {
  const response = await api
    .get<KlawApiResponse<"showActivityLog">>(
      API_PATHS.showActivityLog,
      new URLSearchParams({
        pageNo: params.pageNo,
        ...(params.env && { env: params.env }),
      })
    )
    .then(activityLogTransformer);

  return response;
};

export { getActivityLog, getRequestsStatistics, getRequestsWaitingForApproval };
