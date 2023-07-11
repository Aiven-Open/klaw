import { Banner, Box, Button, GridItem } from "@aivenio/aquarium";
import { TopicOverview } from "src/domain/topic";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";
import { useNavigate } from "react-router-dom";

interface TopicPromotionBannerProps {
  topicPromotionDetails: TopicOverview["topicPromotionDetails"];
  hasOpenRequest: boolean;
  isTopicOwner: boolean;
}

const TopicPromotionBanner = ({
  isTopicOwner,
  topicPromotionDetails,
  hasOpenRequest,
}: TopicPromotionBannerProps) => {
  const navigate = useNavigate();
  const { status, targetEnv, sourceEnv, targetEnvId, topicName } =
    topicPromotionDetails;

  const showRequestPromotionBanner =
    isTopicOwner &&
    status !== "NO_PROMOTION" &&
    status !== "NOT_AUTHORIZED" &&
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
          <Button.Primary
            onClick={() =>
              navigate(
                `/topic/${topicName}/request-promotion?sourceEnv=${sourceEnv}&targetEnv=${targetEnvId}`
              )
            }
          >
            Promote
          </Button.Primary>
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
          <Button.Primary
            onClick={() =>
              navigate(
                `/requests/topics?search=${topicName}&status=CREATED&page=1`
              )
            }
          >
            See the request
          </Button.Primary>
        </Banner>
      </GridItem>
    );
  }

  return null;
};

export { TopicPromotionBanner };
