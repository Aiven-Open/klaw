import {
  Banner,
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
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";
import { getTopicStats } from "src/app/features/topics/details/utils";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";
import { getTopicRequests } from "src/domain/topic";

function TopicOverview() {
  const user = useAuthContext();

  const { topicName, environmentId, topicOverview, topicSchemas } =
    useTopicDetails();

  const {
    topicInfo: { topicOwner },
    topicPromotionDetails: {
      status: promotionStatus,
      targetEnv,
      targetEnvId,
      sourceEnv,
    },
  } = topicOverview;

  const stats = useMemo(() => getTopicStats(topicOverview), [topicOverview]);

  const {
    data: existingPromotionRequest,
    isLoading: isLoadingExistingPromotionRequest,
  } = useQuery(["getTopicRequests", topicName, targetEnvId], {
    queryFn: () =>
      getTopicRequests({
        pageNo: "1",
        search: topicName || "",
        env: targetEnvId || "",
        operationType: "PROMOTE",
      }),
    select: ({ entries, totalPages }) => {
      if (totalPages === 0 || entries[0].requestOperationType !== "PROMOTE") {
        return undefined;
      }
      return {
        requestor: entries[0].requestor,
        teamName: entries[0].teamname,
      };
    },
  });

  if (isLoadingExistingPromotionRequest) {
    return (
      <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
        <div className={"visually-hidden"}>Loading topic details</div>
        <Icon icon={loading} fontSize={"30px"} />
      </Box>
    );
  }

  const showRequestPromotionBanner =
    topicOwner &&
    promotionStatus !== "NO_PROMOTION" &&
    existingPromotionRequest === undefined;
  const showApprovePromotionBanner =
    existingPromotionRequest !== undefined &&
    user?.username !== existingPromotionRequest.requestor;
  const showSeePromotionBanner =
    existingPromotionRequest !== undefined &&
    user?.username === existingPromotionRequest.requestor;

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
      {showRequestPromotionBanner && (
        <GridItem colSpan={"span-2"}>
          <Banner image={illustration} layout="vertical" title={""}>
            <Box element={"p"} marginBottom={"l1"}>
              This schema has not yet been promoted to the {targetEnv}{" "}
              environment.
            </Box>
            <Link
              to={`/topic/${topicName}/request-promotion?sourceEnv=${sourceEnv}&targetEnv=${targetEnvId}`}
            >
              <Button.Primary>Promote</Button.Primary>
            </Link>
          </Banner>
        </GridItem>
      )}
      {showApprovePromotionBanner && (
        <GridItem colSpan={"span-2"}>
          <Banner image={illustration} layout="vertical" title={""}>
            <Box element={"p"} marginBottom={"l1"}>
              A promotion request has already been created by{" "}
              {existingPromotionRequest.requestor} from the team{" "}
              {existingPromotionRequest.teamName}.
            </Box>
            <Link
              to={`/approvals/topics?search=${topicName}&page=1&requestType=PROMOTE`}
            >
              <Button.Primary>Approve the request</Button.Primary>
            </Link>
          </Banner>
        </GridItem>
      )}
      {showSeePromotionBanner && (
        <GridItem colSpan={"span-2"}>
          <Banner image={illustration} layout="vertical" title={""}>
            <Box element={"p"} marginBottom={"l1"}>
              You have created a promotion request for {topicName}.
            </Box>
            <Link
              to={`/requests/topics?search=${topicName}&page=1&requestType=PROMOTE`}
            >
              <Button.Primary>See the request</Button.Primary>
            </Link>
          </Banner>
        </GridItem>
      )}

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
