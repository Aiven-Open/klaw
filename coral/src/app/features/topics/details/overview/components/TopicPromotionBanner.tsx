import { Banner, Box, Button, GridItem } from "@aivenio/aquarium";
import { Link } from "react-router-dom";
import { TopicOverview } from "src/domain/topic";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";

interface TopicPromotionBannerProps {
  topicPromotionDetails: TopicOverview["topicPromotionDetails"];
  hasOpenRequest: boolean;
  isTopicOwner?: boolean;
}

const TopicPromotionBanner = ({
  isTopicOwner,
  topicPromotionDetails,
  hasOpenRequest,
}: TopicPromotionBannerProps) => {
  const { status, targetEnv, sourceEnv, targetEnvId, topicName } =
    topicPromotionDetails;

  const showRequestPromotionBanner =
    isTopicOwner &&
    status !== "NO_PROMOTION" &&
    !hasOpenRequest &&
    targetEnv !== undefined &&
    sourceEnv !== undefined &&
    targetEnvId !== undefined;

  if (showRequestPromotionBanner) {
    return (
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
    );
  }

  if (hasOpenRequest) {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box element={"p"} marginBottom={"l1"}>
            There is an open request for {topicName}.
          </Box>
          <Link
            to={`/requests/topics?search=${topicName}&status=CREATED&page=1`}
          >
            <Button.Primary>See the request</Button.Primary>
          </Link>
        </Banner>
      </GridItem>
    );
  }

  return null;
};

export { TopicPromotionBanner };
