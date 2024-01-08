import { transformGetActivityLogForTeamOverviewResponse } from "src/domain/analytics/analytics-transformer";
import { KlawApiResponse } from "types/utils";

const mockedApiResponse: KlawApiResponse<"getActivityLogForTeamOverview"> = {
  topicsPerTeamPerEnvOverview: {
    data: [128, 32, 1],
    labels: ["DEV", "TST", "PRD"],
    colors: ["Green", "Green", "Green"],
    options: {
      title: {
        display: true,
        text: "Topics per cluster (Ospo) (Total 161)",
        position: "bottom",
        fontColor: "red",
      },
    },
    titleForReport: "Topics per cluster (Ospo)",
    xaxisLabel: "Clusters",
    yaxisLabel: "Topics",
  },
  activityLogOverview: {
    data: [
      3, 2, 1, 2, 1, 1, 5, 3, 2, 1, 7, 2, 7, 32, 1, 6, 7, 2, 1, 3, 3, 4, 3, 2,
      2, 3, 10, 7, 3, 1,
    ],
    labels: [
      "17-Jun-2023",
      "20-Jun-2023",
      "21-Jun-2023",
      "23-Jun-2023",
      "26-Jun-2023",
      "27-Jun-2023",
      "28-Jun-2023",
      "29-Jun-2023",
      "30-Jun-2023",
      "03-Jul-2023",
      "04-Jul-2023",
      "05-Jul-2023",
      "06-Jul-2023",
      "07-Jul-2023",
      "10-Jul-2023",
      "11-Jul-2023",
      "12-Jul-2023",
      "13-Jul-2023",
      "14-Jul-2023",
      "17-Jul-2023",
      "19-Jul-2023",
      "20-Jul-2023",
      "21-Jul-2023",
      "24-Jul-2023",
      "25-Jul-2023",
      "26-Jul-2023",
      "27-Jul-2023",
      "28-Jul-2023",
      "31-Jul-2023",
      "02-Aug-2023",
    ],
    colors: [
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
      "Green",
    ],
    options: {
      title: {
        display: true,
        text: "Requests per day (Ospo) (Total 127)",
        position: "bottom",
        fontColor: "red",
      },
    },
    titleForReport: "Requests per day (Ospo)",
    xaxisLabel: "Days",
    yaxisLabel: "Activities",
  },
};

const expected = {
  requestsPerDay: [
    {
      date: "17-Jun-2023",
      Requests: 3,
    },
    {
      date: "20-Jun-2023",
      Requests: 2,
    },
    {
      date: "21-Jun-2023",
      Requests: 1,
    },
    {
      date: "23-Jun-2023",
      Requests: 2,
    },
    {
      date: "26-Jun-2023",
      Requests: 1,
    },
    {
      date: "27-Jun-2023",
      Requests: 1,
    },
    {
      date: "28-Jun-2023",
      Requests: 5,
    },
    {
      date: "29-Jun-2023",
      Requests: 3,
    },
    {
      date: "30-Jun-2023",
      Requests: 2,
    },
    {
      date: "03-Jul-2023",
      Requests: 1,
    },
    {
      date: "04-Jul-2023",
      Requests: 7,
    },
    {
      date: "05-Jul-2023",
      Requests: 2,
    },
    {
      date: "06-Jul-2023",
      Requests: 7,
    },
    {
      date: "07-Jul-2023",
      Requests: 32,
    },
    {
      date: "10-Jul-2023",
      Requests: 1,
    },
    {
      date: "11-Jul-2023",
      Requests: 6,
    },
    {
      date: "12-Jul-2023",
      Requests: 7,
    },
    {
      date: "13-Jul-2023",
      Requests: 2,
    },
    {
      date: "14-Jul-2023",
      Requests: 1,
    },
    {
      date: "17-Jul-2023",
      Requests: 3,
    },
    {
      date: "19-Jul-2023",
      Requests: 3,
    },
    {
      date: "20-Jul-2023",
      Requests: 4,
    },
    {
      date: "21-Jul-2023",
      Requests: 3,
    },
    {
      date: "24-Jul-2023",
      Requests: 2,
    },
    {
      date: "25-Jul-2023",
      Requests: 2,
    },
    {
      date: "26-Jul-2023",
      Requests: 3,
    },
    {
      date: "27-Jul-2023",
      Requests: 10,
    },
    {
      date: "28-Jul-2023",
      Requests: 7,
    },
    {
      date: "31-Jul-2023",
      Requests: 3,
    },
    {
      date: "02-Aug-2023",
      Requests: 1,
    },
  ],
  topicsPerEnv: [
    {
      environment: "DEV",
      Topics: 128,
    },
    {
      environment: "TST",
      Topics: 32,
    },
    {
      environment: "PRD",
      Topics: 1,
    },
  ],
};

describe("analytics-transformer.ts", () => {
  describe("transformGetActivityLogForTeamOverviewResponse", () => {
    it("transforms a getActivityLogForTeamOverview into DashboardsAnalyticsData", () => {
      const result =
        transformGetActivityLogForTeamOverviewResponse(mockedApiResponse);

      expect(result).toStrictEqual(expected);
    });

    it("returns empty array when requestsPerDay is invalid", () => {
      const result = transformGetActivityLogForTeamOverviewResponse({
        ...mockedApiResponse,
        activityLogOverview: {
          ...mockedApiResponse.activityLogOverview,
          labels: [],
        },
      });

      expect(result).toStrictEqual({ ...expected, requestsPerDay: [] });
    });

    it("returns empty array when topicsPerEnv is invalid", () => {
      const result = transformGetActivityLogForTeamOverviewResponse({
        ...mockedApiResponse,
        topicsPerTeamPerEnvOverview: {
          ...mockedApiResponse.topicsPerTeamPerEnvOverview,
          labels: [],
        },
      });

      expect(result).toStrictEqual({ ...expected, topicsPerEnv: [] });
    });
  });
});
