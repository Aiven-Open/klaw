import { Banner, Box, Button, GridItem } from "@aivenio/aquarium";
import { Link } from "react-router-dom";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { TopicOverview } from "src/domain/topic";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";

interface TopicPromotionBannerProps {
  topicPromotionDetails: TopicOverview["topicPromotionDetails"];
  isTopicOwner?: boolean;
  existingPromotionRequest?: {
    requestor: string;
    teamName: string;
  };
}

const TopicPromotionBanner = ({
  isTopicOwner,
  topicPromotionDetails,
  existingPromotionRequest,
}: TopicPromotionBannerProps) => {
  const user = useAuthContext();
  const { status, targetEnv, sourceEnv, targetEnvId, topicName } =
    topicPromotionDetails;

  const showRequestPromotionBanner =
    isTopicOwner &&
    status !== "NO_PROMOTION" &&
    existingPromotionRequest === undefined &&
    targetEnv !== undefined &&
    sourceEnv !== undefined &&
    targetEnvId !== undefined;
  const showApprovePromotionBanner =
    existingPromotionRequest !== undefined &&
    user?.username !== existingPromotionRequest.requestor;
  const showSeePromotionBanner =
    existingPromotionRequest !== undefined &&
    user?.username === existingPromotionRequest.requestor;

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

  if (showApprovePromotionBanner) {
    const { requestor, teamName } = existingPromotionRequest;

    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box element={"p"} marginBottom={"l1"}>
            A promotion request has already been created by {requestor} from the
            team {teamName}.
          </Box>
          <Link
            to={`/approvals/topics?search=${topicName}&page=1&requestType=PROMOTE`}
          >
            <Button.Primary>Approve the request</Button.Primary>
          </Link>
        </Banner>
      </GridItem>
    );
  }

  if (showSeePromotionBanner) {
    return (
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
    );
  }

  return null;
};

export { TopicPromotionBanner };
