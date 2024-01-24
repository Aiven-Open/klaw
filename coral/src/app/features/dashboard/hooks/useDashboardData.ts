import { useQuery } from "@tanstack/react-query";
import {
  DashboardsAnalyticsData,
  getActivityLogForTeamOverview,
  getDashboardStats,
} from "src/domain/analytics";
import { AuthUser, getAuth } from "src/domain/auth-user";
import { getRequestsWaitingForApproval } from "src/domain/requests";

type DashboardData = {
  chartsData?: DashboardsAnalyticsData;
  statsData: {
    producerCount?: number;
    consumerCount?: number;
    totalTeamTopics?: number;
    totalOrgTopics?: number;
    pendingTopicRequests?: number;
    pendingAclRequests?: number;
    pendingSchemaRequests?: number;
    pendingConnectorRequests?: number;
  };
};

export const useDashboardData = ({
  numberOfDays,
}: {
  numberOfDays: string;
}): {
  data: DashboardData;
  isLoading: {
    chartsData: boolean;
    statsData: boolean;
    pendingRequestsData: boolean;
  };
  isError: boolean;
  error: string;
} => {
  const {
    data: chartsData,
    isLoading: isLoadingChartsData,
    isError: isErrorChartsData,
  } = useQuery<DashboardsAnalyticsData, Error>(
    ["getActivityLogForTeamOverview", numberOfDays],
    {
      queryFn: () =>
        getActivityLogForTeamOverview({
          numberOfDays: Number(numberOfDays),
        }),
      // keepPreviousData: true,
    }
  );

  const {
    data: statsData,
    isLoading: isLoadingStatsData,
    isError: isErrorStatsData,
  } = useQuery(["getDashboardStats"], {
    queryFn: getDashboardStats,
  });

  const {
    data: pendingRequestsData,
    isLoading: isLoadingPendingRequestsData,
    isError: isErrorPendingRequestsData,
  } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  const {
    data: authUserData,
    isLoading: isLoadingAuthUserData,
    isError: isErrorAuthUserData,
  } = useQuery<AuthUser | undefined>(["user-getAuth-data"], getAuth);

  return {
    data: {
      chartsData,
      statsData: {
        ...statsData,
        totalTeamTopics: authUserData?.totalTeamTopics,
        totalOrgTopics: authUserData?.totalOrgTopics,
        pendingTopicRequests: pendingRequestsData?.TOPIC,
        pendingAclRequests: pendingRequestsData?.ACL,
        pendingSchemaRequests: pendingRequestsData?.SCHEMA,
        pendingConnectorRequests: pendingRequestsData?.CONNECTOR,
      },
    },
    isLoading: {
      chartsData: isLoadingChartsData,
      statsData: isLoadingStatsData || isLoadingAuthUserData,
      pendingRequestsData: isLoadingPendingRequestsData,
    },
    isError:
      isErrorChartsData ||
      isErrorStatsData ||
      isErrorAuthUserData ||
      isErrorPendingRequestsData,
    error: "Dashboard data could not be loaded",
  };
};
