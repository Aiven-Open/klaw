import zipWith from "lodash/zipWith";
import { DashboardsAnalyticsData } from "src/domain/analytics/analytics-types";
import { KlawApiResponse } from "types/utils";

const isValidData = (
  array: (string | number)[] | undefined
): array is string[] | number[] => {
  return (
    array !== undefined &&
    array.every((element: string | number) => element !== undefined)
  );
};

const validateData = (dataToValidate: {
  labels: string[] | undefined;
  data: number[] | undefined;
}): dataToValidate is {
  labels: string[];
  data: number[];
} => {
  const { labels, data } = dataToValidate;

  return (
    labels !== undefined &&
    data !== undefined &&
    labels.length === data.length &&
    isValidData(labels) &&
    isValidData(data)
  );
};

const transformGetActivityLogForTeamOverviewResponse = (
  data: KlawApiResponse<"getActivityLogForTeamOverview">
): DashboardsAnalyticsData => {
  const requestsData = {
    labels: data.activityLogOverview?.labels,
    data: data.activityLogOverview?.data,
  };
  const topicsPerEnvData = {
    labels: data.topicsPerTeamPerEnvOverview?.labels,
    data: data.topicsPerTeamPerEnvOverview?.data,
  };

  const requestsPerDay = validateData(requestsData)
    ? zipWith(
        requestsData.labels,
        requestsData.data,
        (date: string, Requests: number) => {
          return { date, Requests };
        }
      )
    : [];

  const topicsPerEnv = validateData(topicsPerEnvData)
    ? zipWith(
        topicsPerEnvData.labels,
        topicsPerEnvData.data,
        (environment: string, Topics: number) => {
          return { environment, Topics };
        }
      )
    : [];

  return { requestsPerDay, topicsPerEnv };
};

export { transformGetActivityLogForTeamOverviewResponse };
