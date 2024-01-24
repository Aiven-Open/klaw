import {
  Box,
  Card,
  Grid,
  Icon,
  NativeSelect,
  Option,
  Template,
  useToast,
} from "@aivenio/aquarium";
import { AreaChart, Axis, BarChart, Tooltip } from "@aivenio/aquarium/charts";
import loading from "@aivenio/aquarium/icons/loading";
import { useEffect, useState } from "react";
import StatsDisplay from "src/app/components/StatsDisplay";
import { useDashboardData } from "src/app/features/dashboard/hooks/useDashboardData";
import loadingIcon from "@aivenio/aquarium/dist/src/icons/loading";

const Dashboard = () => {
  const [numberOfDays, setNumberOfDays] = useState("30");
  const {
    data: { chartsData, statsData },
    isLoading,
    isError,
    error,
  } = useDashboardData({ numberOfDays });

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
            isLoading={isLoading.statsData}
            amount={statsData.totalTeamTopics}
            entity={"My team's topics"}
          />
          <StatsDisplay
            isLoading={isLoading.statsData}
            amount={statsData.totalOrgTopics}
            entity={"My organization's topics"}
          />
          <StatsDisplay
            isLoading={isLoading.statsData}
            amount={statsData.producerCount}
            entity={"My producer topics"}
          />
          <StatsDisplay
            isLoading={isLoading.statsData}
            amount={statsData.consumerCount}
            entity={"My consumer topics"}
          />
        </Grid>
      </Card>
      <Card title="My team's pending requests" fullWidth>
        <Grid cols={"4"}>
          <StatsDisplay
            isLoading={isLoading.pendingRequestsData}
            amount={statsData.pendingTopicRequests}
            entity={"Topic requests"}
          />
          <StatsDisplay
            isLoading={isLoading.pendingRequestsData}
            amount={statsData.pendingAclRequests}
            entity={"ACL requests"}
          />
          <StatsDisplay
            isLoading={isLoading.pendingRequestsData}
            amount={statsData.pendingSchemaRequests}
            entity={"Schema requests"}
          />
          <StatsDisplay
            isLoading={isLoading.pendingRequestsData}
            amount={statsData.pendingConnectorRequests}
            entity={"Connector requests"}
          />
        </Grid>
      </Card>

      <Card title="My team's approved requests" fullWidth>
        <NativeSelect
          labelText={"Timeframe"}
          defaultValue={"30"}
          value={numberOfDays}
          onChange={(e) => setNumberOfDays(e.target.value)}
        >
          <Option key={"7"} value={"7"}>
            Last 7 days <Icon icon={loadingIcon} />
          </Option>
          <Option key={"30"} value={"30"}>
            Last 30 days
          </Option>
          <Option key={"90"} value={"90"}>
            Last 90 days
          </Option>
        </NativeSelect>
        {isLoading.chartsData || forceLoading ? (
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
        {isLoading.chartsData || forceLoading ? (
          <Box.Flex
            justifyContent={"center"}
            alignContent={"center"}
            style={{ minHeight: "250px" }}
            flexWrap={"wrap"}
          >
            <Icon icon={loading} fontSize={"30px"} />
          </Box.Flex>
        ) : (
          <BarChart height={250} data={chartsData?.topicsPerEnv}>
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
