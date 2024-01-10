import { transformGetActivityLogForTeamOverviewResponse } from "src/domain/analytics/analytics-transformer";
import { DashboardsAnalyticsData } from "src/domain/analytics/analytics-types";
import api, { API_PATHS } from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getActivityLogForTeamOverview = async (
  activityLogForTeam: KlawApiRequestQueryParameters<"getActivityLogForTeamOverview"> = {
    activityLogForTeam: "true",
  }
): Promise<DashboardsAnalyticsData> => {
  const response = api
    .get<KlawApiResponse<"getActivityLogForTeamOverview">>(
      API_PATHS.getActivityLogForTeamOverview,
      new URLSearchParams(activityLogForTeam)
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
