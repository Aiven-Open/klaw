import { Box, Card, Grid, Icon, Template, useToast } from "@aivenio/aquarium";
import { AreaChart, Axis, BarChart, Tooltip } from "@aivenio/aquarium/charts";
import loading from "@aivenio/aquarium/icons/loading";
import { useEffect, useState } from "react";
import StatsDisplay from "src/app/components/StatsDisplay";
import { useDashboardData } from "src/app/features/dashboard/hooks/useDashboardData";

const Dashboard = () => {
  const {
    data: { chartsData, statsData },
    isLoading,
    isError,
    error,
  } = useDashboardData();

  const toast = useToast();

  useEffect(() => {
    if (isError) {
      toast({
        message: error,
        position: "bottom-left",
        variant: "default",
      });
    }
  }, [isError]);

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
        <Grid cols={"4"}>
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.totalTeamTopics}
            entity={"My team's topics"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.totalOrgTopics}
            entity={"My organization's topics"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.producerCount}
            entity={"My producer topics"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.consumerCount}
            entity={"My consumer topics"}
          />
        </Grid>
      </Card>
      <Card title="My team's pending requests" fullWidth>
        <Grid cols={"4"}>
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.pendingTopicRequests}
            entity={"Topic requests"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.pendingAclRequests}
            entity={"ACL requests"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.pendingSchemaRequests}
            entity={"Schema requests"}
          />
          <StatsDisplay
            isLoading={isLoading}
            amount={statsData.pendingConnectorRequests}
            entity={"Connector requests"}
          />
        </Grid>
      </Card>

      <Card title="My team's requests per day" fullWidth>
        {isLoading || forceLoading ? (
          <Box.Flex
            justifyContent={"center"}
            alignContent={"center"}
            style={{ minHeight: "250px" }}
            flexWrap={"wrap"}
          >
            <Icon icon={loading} fontSize={"30px"} />
          </Box.Flex>
        ) : (
          <AreaChart height={250} data={chartsData?.requestsPerDay}>
            <Axis.XAxis dataKey="date" interval={"equidistantPreserveStart"} />
            <Axis.YAxis width={10} />
            <AreaChart.Area dataKey="Requests" />
            <Tooltip />
          </AreaChart>
        )}
      </Card>

      <Card title="My team's topics per environment" fullWidth>
        {isLoading || forceLoading ? (
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
            data={chartsData?.topicsPerEnv}
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
