import { Button } from "@aivenio/aquarium";
import { TopicOverview } from "src/domain/topic";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";
import { useNavigate } from "react-router-dom";

interface TopicPromotionBannerProps {
  topicPromotionDetails: TopicOverview["topicPromotionDetails"];
  /** In case there is an open request for this topic
   * it can't be promoted, and we want to show that
   * information to the user.
   */
  hasOpenTopicRequest: boolean;
  topicName: string;
}

const TopicPromotionBanner = ({
  topicPromotionDetails,
  hasOpenTopicRequest,
  topicName,
}: TopicPromotionBannerProps) => {
  const navigate = useNavigate();

  return (
    <div data-testid={"topic-promotion-banner"}>
      <PromotionBanner
        entityName={topicName}
        promotionDetails={topicPromotionDetails}
        hasOpenRequest={hasOpenTopicRequest}
        type={"topic"}
        promoteElement={
          //  @ TODO DS external link does not support
          // internal links and we don't have a component
          // from DS that supports that yet. We've to use a
          // button that calls navigate() onClick to support
          // routing in Coral
          <Button.Primary
            onClick={() =>
              navigate(
                `/topic/${topicName}/request-promotion?sourceEnv=${topicPromotionDetails.sourceEnv}&targetEnv=${topicPromotionDetails.targetEnvId}`
              )
            }
          >
            Promote
          </Button.Primary>
        }
        hasError={false}
        errorMessage={""}
      />
    </div>
  );
};

export { TopicPromotionBanner };
