import {
  Alert,
  Box,
  Button,
  Card,
  Grid,
  GridItem,
  Typography,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useMemo } from "react";
import { Link } from "react-router-dom";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { getTopicStats } from "src/app/features/topics/details/utils";
import { useQuery } from "@tanstack/react-query";
import {
  getClusterDetails,
  ClusterDetails as ClusterDetailsType,
} from "src/domain/cluster";
import { HTTPError } from "src/services/api";
import { ClusterDetails } from "src/app/features/topics/details/overview/components/ClusterDetails";
import { parseErrorMsg } from "src/services/mutation-utils";

function TopicOverview() {
  const {
    topicName,
    environmentId,
    topicOverview,
    topicOverviewIsRefetching,
    topicSchemas,
    topicSchemasIsRefetching,
  } = useTopicDetails();

  const {
    topicInfo: { topicOwner = false, hasOpenTopicRequest, clusterId },
    topicPromotionDetails,
  } = topicOverview;

  const stats = useMemo(() => getTopicStats(topicOverview), [topicOverview]);

  const {
    data: clusterDetails,
    isLoading: clusterDetailsIsLoading,
    isRefetching: clusterDetailsIsRefetching,
    isError: clusterDetailsIsError,
    error: clusterDetailsError,
  } = useQuery<ClusterDetailsType, HTTPError>({
    queryKey: ["cluster-details", String(clusterId)],
    queryFn: () => getClusterDetails(String(clusterId)),
  });

  return (
    <Grid cols={"2"} rows={"2"} gap={"l2"} style={{ gridTemplateRows: "auto" }}>
      <GridItem colSpan={"span-2"}>
        <Card title="Topic details" fullWidth>
          <Box.Flex display="flex" gap={"l7"}>
            <StatsDisplay
              isLoading={topicOverviewIsRefetching}
              amount={stats.replicas}
              entity={"Replicas"}
            />
            <StatsDisplay
              isLoading={topicOverviewIsRefetching}
              amount={stats.partitions}
              entity={"Partitions"}
            />
          </Box.Flex>
        </Card>
      </GridItem>

      {!topicOverviewIsRefetching && topicOwner && (
        <TopicPromotionBanner
          topicName={topicName}
          topicPromotionDetails={topicPromotionDetails}
          hasOpenTopicRequest={hasOpenTopicRequest}
        />
      )}

      <GridItem colSpan={"span-2"}>
        <Card title={"Cluster details"} fullWidth>
          {clusterDetailsIsError && (
            <Alert type={"error"}>
              <>There was an error while loading cluster details:</>
              <br />
              {clusterDetailsError && parseErrorMsg(clusterDetailsError)}
            </Alert>
          )}

          <ClusterDetails
            clusterDetails={clusterDetails}
            isUpdating={
              clusterDetailsIsLoading ||
              topicOverviewIsRefetching ||
              clusterDetailsIsRefetching
            }
          />
        </Card>
      </GridItem>

      <Card title={"Subscriptions"} fullWidth>
        <Box.Flex gap={"l7"}>
          <StatsDisplay
            isLoading={topicOverviewIsRefetching}
            amount={stats.producers}
            entity={"Producers"}
          />
          <StatsDisplay
            isLoading={topicOverviewIsRefetching}
            amount={stats.consumers}
            entity={"Consumers"}
          />
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
          isLoading={topicSchemasIsRefetching}
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
