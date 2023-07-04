import {
  Box,
  Button,
  Card,
  Grid,
  GridItem,
  Icon,
  Typography,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import loading from "@aivenio/aquarium/icons/loading";
import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { Link } from "react-router-dom";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { getTopicStats } from "src/app/features/topics/details/utils";
import { getTopicRequests } from "src/domain/topic";

function TopicOverview() {
  const { topicName, environmentId, topicOverview, topicSchemas } =
    useTopicDetails();

  const {
    topicInfo: { topicOwner },
    topicPromotionDetails,
  } = topicOverview;

  const stats = useMemo(() => getTopicStats(topicOverview), [topicOverview]);

  const {
    data: existingPromotionRequest,
    isLoading: isLoadingExistingPromotionRequest,
  } = useQuery(
    ["getTopicRequests", topicName, topicPromotionDetails.targetEnvId],
    {
      queryFn: () =>
        getTopicRequests({
          pageNo: "1",
          search: topicName || "",
          env: topicPromotionDetails.targetEnvId || "",
          operationType: "PROMOTE",
        }),
      select: ({ entries, totalPages }) => {
        if (totalPages === 0 || entries[0].requestOperationType !== "PROMOTE") {
          return undefined;
        }
        return {
          requestor: entries[0].requestor,
          teamName: entries[0].teamname,
          status: entries[0].requestStatus,
        };
      },
    }
  );

  if (isLoadingExistingPromotionRequest) {
    return (
      <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
        <div className={"visually-hidden"}>Loading topic details</div>
        <Icon icon={loading} fontSize={"30px"} />
      </Box>
    );
  }

  return (
    <Grid cols={"2"} rows={"2"} gap={"l2"} style={{ gridTemplateRows: "auto" }}>
      <GridItem colSpan={"span-2"}>
        <Card title="Topic details" fullWidth>
          <Box.Flex display="flex" gap={"l7"}>
            <StatsDisplay amount={stats.replicas} entity={"Replicas"} />
            <StatsDisplay amount={stats.partitions} entity={"Partitions"} />
          </Box.Flex>
        </Card>
      </GridItem>

      <TopicPromotionBanner
        topicPromotionDetails={topicPromotionDetails}
        isTopicOwner={topicOwner}
        existingPromotionRequest={existingPromotionRequest}
      />

      <Card title={"Subscriptions"} fullWidth>
        <Box.Flex gap={"l7"}>
          <StatsDisplay amount={stats.producers} entity={"Producers"} />
          <StatsDisplay amount={stats.consumers} entity={"Consumers"} />
        </Box.Flex>
        <Box.Flex flexDirection={"row"} gap={"l3"} paddingTop={"l2"}>
          <Link to={`/topic/${topicName}/subscribe?env=${environmentId}`}>
            <Button.Ghost icon={add}>
              <Typography.SmallStrong color={"primary-80"}>
                Request new subscription
              </Typography.SmallStrong>
            </Button.Ghost>
          </Link>
          <Link to={`/topic/${topicName}/subscriptions`}>
            <Button.Ghost>
              <Typography.SmallStrong color={"primary-80"}>
                See subscriptions
              </Typography.SmallStrong>
            </Button.Ghost>
          </Link>
        </Box.Flex>
      </Card>
      <Card title={"Schemas"} fullWidth>
        <StatsDisplay
          amount={
            topicSchemas.allSchemaVersions !== undefined
              ? Object.keys(topicSchemas.allSchemaVersions).length
              : 0
          }
          entity={"Schemas"}
        />
        <Box.Flex flexDirection={"row"} gap={"l3"} paddingTop={"l2"}>
          <Link to={`/topic/${topicName}/request-schema`}>
            <Button.Ghost icon={add}>
              <Typography.SmallStrong color={"primary-80"}>
                Request new schema
              </Typography.SmallStrong>
            </Button.Ghost>
          </Link>
          <Link to={`/topic/${topicName}/schema`}>
            <Button.Ghost>
              <Typography.SmallStrong color={"primary-80"}>
                See schema
              </Typography.SmallStrong>
            </Button.Ghost>
          </Link>
        </Box.Flex>
      </Card>
    </Grid>
  );
}

export { TopicOverview };
