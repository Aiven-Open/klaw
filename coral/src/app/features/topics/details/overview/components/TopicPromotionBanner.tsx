import { TopicOverview } from "src/domain/topic";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";

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
        entityName={topicName}
        promotionDetails={topicPromotionDetails}
        hasOpenRequest={hasOpenTopicRequest}
        type={"topic"}
        promoteElement={
          <InternalLinkButton
            to={`/topic/${topicName}/request-promotion?sourceEnv=${topicPromotionDetails.sourceEnv}&targetEnv=${topicPromotionDetails.targetEnvId}`}
          >
            Promote
          </InternalLinkButton>
        }
        hasError={false}
        errorMessage={""}
      />
    </div>
  );
};

export { TopicPromotionBanner };
