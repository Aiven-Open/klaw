import { Button } from "@aivenio/aquarium";
import { TopicOverview } from "src/domain/topic";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";

interface TopicPromotionBannerProps {
  topicPromotionDetails: TopicOverview["topicPromotionDetails"];
  hasOpenRequest: boolean;
  topicName: string;
}

const TopicPromotionBanner = ({
  topicPromotionDetails,
  hasOpenRequest,
  topicName,
}: TopicPromotionBannerProps) => {
  return (
    <div data-testid={"topic-promotion-banner"}>
      <PromotionBanner
        topicName={topicName}
        promotionDetails={topicPromotionDetails}
        hasOpenRequest={hasOpenRequest}
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
