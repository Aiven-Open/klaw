import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";

//@TODO replace when api is ready
// eslint-disable-next-line import/exports-last
export type TemporaryConnectorPromotionDetails = {
  status:
    | "SUCCESS"
    | "NOT_AUTHORIZED"
    | "REQUEST_OPEN"
    | "NO_PROMOTION"
    | "FAILURE";
  sourceEnv?: string;
  targetEnv?: string;
  targetEnvId?: string;
  error?: string;
  connectorName?: string;
  sourceConnectorConfig?: string;
};
interface ConnectorPromotionBannerProps {
  connectorPromotionDetails: TemporaryConnectorPromotionDetails;
  /** In case there is an open request for this connector
   * it can't be promoted, and we want to show that
   * information to the user.
   */
  hasOpenConnectorRequest: boolean;
  hasOpenClaimRequest: boolean;
  connectorName: string;
}

const ConnectorPromotionBanner = ({
  connectorPromotionDetails,
  hasOpenClaimRequest,
  hasOpenConnectorRequest,
  connectorName,
}: ConnectorPromotionBannerProps) => {
  return (
    <div data-testid={"connector-promotion-banner"}>
      <PromotionBanner
        entityName={connectorName}
        promotionDetails={connectorPromotionDetails}
        hasOpenRequest={hasOpenConnectorRequest}
        hasOpenClaimRequest={hasOpenClaimRequest}
        type={"connector"}
        promoteElement={
          <InternalLinkButton
            to={`/connector/${connectorName}/request-promotion?sourceEnv=${connectorPromotionDetails.sourceEnv}&targetEnv=${connectorPromotionDetails.targetEnvId}`}
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

export { ConnectorPromotionBanner };
