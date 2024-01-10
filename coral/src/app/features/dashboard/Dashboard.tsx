import { Alert, Box, Card, Grid, Icon, Template } from "@aivenio/aquarium";
import { AreaChart, Axis, BarChart, Tooltip } from "@aivenio/aquarium/charts";
import loading from "@aivenio/aquarium/icons/loading";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import StatsDisplay from "src/app/components/StatsDisplay";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { usePendingRequests } from "src/app/hooks/usePendingRequests";
import {
  DashboardsAnalyticsData,
  getActivityLogForTeamOverview,
  getDashboardStats,
} from "src/domain/analytics";
import { parseErrorMsg } from "src/services/mutation-utils";

const Dashboard = () => {
  const {
    data: chartData,
    isLoading: isLoadingChartData,
    isError: isErrorChartData,
    error: errorChartData,
  } = useQuery<DashboardsAnalyticsData, Error>(
    ["getActivityLogForTeamOverview"],
    {
      queryFn: () => getActivityLogForTeamOverview(),
    }
  );

  const {
    data: statsData,
    isLoading: isLoadingStatsData,
    isError: isErrorStatsData,
    error: errorStatsData,
  } = useQuery(["getDashboardStats"], {
    queryFn: getDashboardStats,
  });

  const { TOPIC, ACL, SCHEMA, CONNECTOR } = usePendingRequests();

  const { totalTeamTopics, totalOrgTopics } = useAuthContext();

  // The following contraption is to allow charts to be responsive despite being rendered in a Card
  // Without it, the size of the charts calculated by the ResponsiveContainer they are crapped in under the hood
  // will only be calculated on the first render, which makes the entire Dashboard layout unresponsive on viewport resize
  // This will remove the Chart components and show the loading state while resizing, and then render them again with the proper size
  // Source: https://github.com/recharts/recharts/issues/1423#issuecomment-538670179
  const [forceLoading, setForceLoading] = useState(false);
  useEffect(() => {
    let resizeThrottleTimeout: NodeJS.Timeout;
    let skip = false;
    const resizeCb = () => {
      if (skip) {
        return;
      }

      setForceLoading(true);
      skip = true;
      resizeThrottleTimeout = setTimeout(() => {
        setForceLoading(false);
        skip = false;
      }, 100);
    };
    window.addEventListener("resize", resizeCb);

    return () => {
      clearTimeout(resizeThrottleTimeout);
    };
  }, [setForceLoading]);

  return (
    <Template columns={1} gap={"l2"}>
      <Card title="Topics" fullWidth>
        {isErrorStatsData && (
          <Box marginBottom={"l1"}>
            <Alert type={"error"}>
              Could not gather the topics data: {parseErrorMsg(errorStatsData)}
            </Alert>
          </Box>
        )}
        <Grid cols={"4"}>
          <StatsDisplay
            isLoading={false}
            amount={totalTeamTopics}
            entity={"My team's topics"}
          />
          <StatsDisplay
            isLoading={false}
            amount={totalOrgTopics}
            entity={"My organization's topics"}
          />
          <StatsDisplay
            isLoading={isLoadingStatsData}
            amount={statsData?.producerCount}
            entity={"My producer topics"}
          />
          <StatsDisplay
            isLoading={isLoadingStatsData}
            amount={statsData?.consumerCount}
            entity={"My consumer topics"}
          />
        </Grid>
      </Card>
      <Card title="My team's pending requests" fullWidth>
        <Grid cols={"4"}>
          <StatsDisplay
            isLoading={TOPIC === undefined}
            amount={TOPIC}
            entity={"Topic requests"}
          />
          <StatsDisplay
            isLoading={ACL === undefined}
            amount={ACL}
            entity={"ACL requests"}
          />
          <StatsDisplay
            isLoading={SCHEMA === undefined}
            amount={SCHEMA}
            entity={"Schema requests"}
          />
          <StatsDisplay
            isLoading={CONNECTOR === undefined}
            amount={CONNECTOR}
            entity={"Connector requests"}
          />
        </Grid>
      </Card>

      <Card title="My team's requests per day" fullWidth>
        {isErrorChartData && (
          <Alert type={"error"}>
            Could not gather the requests per day data :{" "}
            {parseErrorMsg(errorChartData)}
          </Alert>
        )}
        {isLoadingChartData || forceLoading ? (
          <Box.Flex
            justifyContent={"center"}
            alignContent={"center"}
            style={{ minHeight: "250px" }}
            flexWrap={"wrap"}
          >
            <Icon icon={loading} fontSize={"30px"} />
          </Box.Flex>
        ) : (
          <AreaChart height={250} data={chartData?.requestsPerDay}>
            <Axis.XAxis dataKey="date" interval={"equidistantPreserveStart"} />
            <Axis.YAxis width={10} />
            <AreaChart.Area dataKey="Requests" />
            <Tooltip />
          </AreaChart>
        )}
      </Card>

      <Card title="My team's topics per environment" fullWidth>
        {isErrorChartData && (
          <Alert type={"error"}>
            Could not gather the topics per environment data:{" "}
            {parseErrorMsg(errorChartData)}
          </Alert>
        )}
        {isLoadingChartData || forceLoading ? (
          <Box.Flex
            justifyContent={"center"}
            alignContent={"center"}
            style={{ minHeight: "250px" }}
            flexWrap={"wrap"}
          >
            <Icon icon={loading} fontSize={"30px"} />
          </Box.Flex>
        ) : (
          <BarChart
            height={250}
            layout={"vertical"}
            data={chartData?.topicsPerEnv}
          >
            <Axis.XAxis dataKey="environment" />
            <Axis.YAxis width={10} />
            {/* fill is --aquarium-colors-primary-100 */}
            <BarChart.Bar dataKey="Topics" fill="#0788d1" />
            <Tooltip />
          </BarChart>
        )}
      </Card>
    </Template>
  );
};

export default Dashboard;
