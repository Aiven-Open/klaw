import { Button } from "@aivenio/aquarium";
import { TopicSchemaOverview } from "src/domain/topic";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";

interface SchemaPromotionBannerProps {
  schemaPromotionDetails: TopicSchemaOverview["schemaPromotionDetails"];
  /** In case there is an open request for this schema
   * it can't be promoted, and we want to show that
   * information to the user.
   */
  hasOpenSchemaRequest: boolean;
  topicName: string;
  setShowSchemaPromotionModal: () => void;
}

const SchemaPromotionBanner = ({
  schemaPromotionDetails,
  hasOpenSchemaRequest,
  topicName,
  setShowSchemaPromotionModal,
}: SchemaPromotionBannerProps) => {
  return (
    <div data-testid={"schema-promotion-banner"}>
      <PromotionBanner
        entityName={topicName}
        promotionDetails={schemaPromotionDetails}
        hasOpenRequest={hasOpenSchemaRequest}
        type={"schema"}
        promoteElement={
          <Button.Primary onClick={setShowSchemaPromotionModal}>
            Promote
          </Button.Primary>
        }
      />
    </div>
  );
};

export { SchemaPromotionBanner };
