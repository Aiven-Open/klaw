import { GridItem, Banner, Box, Button } from "@aivenio/aquarium";
import { Link } from "react-router-dom";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";

interface PomoteConfig {
  type: "PROMOTE";
  topicName: string;
  targetEnv: string;
  sourceEnv: string;
  targetEnvId: string;
}

interface ApprovePromotionConfig {
  type: "APPROVE_PROMOTION";
  topicName: string;
  requestor: string;
  teamName: string;
}

interface SeePromotionConfig {
  type: "SEE_PROMOTION";
  topicName: string;
}

type TopicPromotionBannerProps =
  | PomoteConfig
  | ApprovePromotionConfig
  | SeePromotionConfig;

const TopicPromotionBanner = (props: TopicPromotionBannerProps) => {
  const { type } = props;

  if (type === "PROMOTE") {
    const { targetEnv, topicName, sourceEnv, targetEnvId } = props;

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

  if (type === "APPROVE_PROMOTION") {
    const { topicName, requestor, teamName } = props;

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

  if (type === "SEE_PROMOTION") {
    const { topicName } = props;

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
