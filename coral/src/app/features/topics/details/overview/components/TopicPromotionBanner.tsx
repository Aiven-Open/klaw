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
    status === "SUCCESS" &&
    targetEnv !== undefined &&
    sourceEnv !== undefined &&
    targetEnvId !== undefined;

  // hasOpenTopicRequest is true for any request open on the current topic in the current environment (source environment)
  if (hasOpenRequest) {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box component={"p"} marginBottom={"l1"}>
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

  // status is "REQUEST_OPEN" when there is a promotion request open on a topic in the target environment for promotion,
  // ie not the current topic + source environment
  if (status === "REQUEST_OPEN") {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box component={"p"} marginBottom={"l1"}>
            There is already an open promotion request for {topicName}.
          </Box>
          <Button.Primary
            onClick={() =>
              navigate(
                `/requests/topics?search=${topicName}&requestType=PROMOTE&status=CREATED&page=1`
              )
            }
          >
            See the request
          </Button.Primary>
        </Banner>
      </GridItem>
    );
  }

  if (showRequestPromotionBanner) {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box component={"p"} marginBottom={"l1"}>
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

  return null;
};

export { TopicPromotionBanner };
