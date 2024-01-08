export interface DashboardsAnalyticsData {
  requestsPerDay: {
    date: string;
    Requests: number;
  }[];
  topicsPerEnv: {
    environment: string;
    Topics: number;
  }[];
}
