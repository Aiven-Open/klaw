import { Box, Card, Template } from "@aivenio/aquarium";
import zip from "lodash/zip";
import StatsDisplay from "src/app/components/StatsDisplay";
import { AreaChart, Axis, BarChart, Tooltip } from "@aivenio/aquarium/charts";

const mockData = {
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

const Dashboard = () => {
  const requestsData = zip(
    mockData.activityLogOverview.labels,
    mockData.activityLogOverview.data
  ).map((zippedElement) => {
    return { date: zippedElement[0], Requests: zippedElement[1] };
  });
  const analyticsData = zip(
    mockData.topicsPerTeamPerEnvOverview.labels,
    mockData.topicsPerTeamPerEnvOverview.data
  ).map((zippedElement) => {
    return { environment: zippedElement[0], Topics: zippedElement[1] };
  });

  return (
    <Template columns={1} gap={"l2"}>
      <Card title="Topics" fullWidth>
        <Box.Flex justifyContent={"space-between"}>
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"My team's topics"}
          />
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"My organization's topics"}
          />
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"My producer topics"}
          />
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"My consumer topics"}
          />
        </Box.Flex>
      </Card>
      <Card title="Pending requests" fullWidth>
        <Box.Flex justifyContent={"space-between"}>
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"Topic requests"}
          />
          <StatsDisplay isLoading={false} amount={1} entity={"ACL requests"} />
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"Schema requests"}
          />
          <StatsDisplay
            isLoading={false}
            amount={1}
            entity={"Connector requests"}
          />
        </Box.Flex>
      </Card>
      <Card title="Requests per day" fullWidth>
        <AreaChart height={250} data={requestsData}>
          <Axis.XAxis dataKey="date" interval={"equidistantPreserveStart"} />
          <Axis.YAxis width={10} />
          <AreaChart.Area dataKey="Requests" />
          <Tooltip />
        </AreaChart>{" "}
      </Card>
      <Card title="Analytics" fullWidth>
        <BarChart height={250} layout={"vertical"} data={analyticsData}>
          <Axis.XAxis dataKey="environment" />
          <Axis.YAxis width={10} />
          {/* fill is --aquarium-colors-primary-100 */}
          <BarChart.Bar dataKey="Topics" fill="#0788d1" />
          <Tooltip />
        </BarChart>
      </Card>
    </Template>
  );
};

export default Dashboard;
