import { RenderResult, cleanup } from "@testing-library/react";
import Dashboard from "src/app/features/dashboard/Dashboard";
import {
  getActivityLogForTeamOverview,
  getDashboardStats,
} from "src/domain/analytics";
import { getRequestsWaitingForApproval } from "src/domain/requests/requests-api";
import { mockResizeObserver } from "src/services/test-utils/mock-resize-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/analytics/analytics-api.ts");
jest.mock("src/domain/requests/requests-api.ts");

const mockGetRequestsWaitingForApproval =
  getRequestsWaitingForApproval as jest.MockedFunction<
    typeof getRequestsWaitingForApproval
  >;

const mockGetActivityLogForTeamOverview =
  getActivityLogForTeamOverview as jest.MockedFunction<
    typeof getActivityLogForTeamOverview
  >;

const mockGetDashboardStats = getDashboardStats as jest.MockedFunction<
  typeof getDashboardStats
>;

const mockChartData = {
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

const mockStatsData = {
  producerCount: 12,
  consumerCount: 11,
  teamMembersCount: 12,
};

const mockPendingRequestsData = {
  TOPIC: 1,
  ACL: 1,
  SCHEMA: 1,
  CONNECTOR: 1,
  OPERATIONAL: 1,
  USER: 1,
  TOTAL_NOTIFICATIONS: 4,
};

describe("Dashboard.tsx", () => {
  let component: RenderResult;

  beforeEach(async () => {
    mockResizeObserver();

    mockGetRequestsWaitingForApproval.mockResolvedValue(
      mockPendingRequestsData
    );
    mockGetActivityLogForTeamOverview.mockResolvedValue(mockChartData);
    mockGetDashboardStats.mockResolvedValue(mockStatsData);

    component = customRender(<Dashboard />, {
      memoryRouter: true,
      queryClient: true,
    });
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("renders correct DOM according to data received from getRequestsWaitingForApproval, getActivityLogForTeamOverview, getDashboardStats", () => {
    expect(component.container).toMatchSnapshot();
  });
});
