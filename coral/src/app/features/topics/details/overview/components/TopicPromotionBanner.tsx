import { Button } from "@aivenio/aquarium";
import { TopicOverview } from "src/domain/topic";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";

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
  return (
    <div data-testid={"topic-promotion-banner"}>
      <PromotionBanner
        topicName={topicName}
        promotionDetails={topicPromotionDetails}
        hasOpenRequest={hasOpenTopicRequest}
        type={"topic"}
        promoteElement={
          <Button.ExternalLink
            href={`/topic/${topicName}/request-promotion?sourceEnv=${topicPromotionDetails.sourceEnv}&targetEnv=${topicPromotionDetails.targetEnvId}`}
          >
            Promote
          </Button.ExternalLink>
        }
      />
    </div>
  );
};

export { TopicPromotionBanner };
