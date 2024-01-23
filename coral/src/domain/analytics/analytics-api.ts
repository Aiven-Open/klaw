import { transformGetActivityLogForTeamOverviewResponse } from "src/domain/analytics/analytics-transformer";
import { DashboardsAnalyticsData } from "src/domain/analytics/analytics-types";
import api, { API_PATHS } from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getActivityLogForTeamOverview = async (
  params: KlawApiRequestQueryParameters<"getActivityLogForTeamOverview"> = {
    activityLogForTeam: "true",
    numberOfDays: 30,
  }
): Promise<DashboardsAnalyticsData> => {
  const parsedParams = new URLSearchParams({
    activityLogForTeam: "true",
    numberOfDays: String(params.numberOfDays),
  });

  const response = api
    .get<KlawApiResponse<"getActivityLogForTeamOverview">>(
      API_PATHS.getActivityLogForTeamOverview,
      parsedParams
    )
    .then(transformGetActivityLogForTeamOverviewResponse);

  return response;
};

const getDashboardStats = async () => {
  const response = api.get<KlawApiResponse<"getDashboardStats">>(
    API_PATHS.getDashboardStats
  );

  return response;
};

export { getActivityLogForTeamOverview, getDashboardStats };
