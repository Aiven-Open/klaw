import { Alert, Banner, Box, Spacing } from "@aivenio/aquarium";
import { ReactElement } from "react";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import illustration from "src/app/images/topic-details-banner-Illustration.svg";
import { PromotionStatus } from "src/domain/promotion";

type RequestTypeLocal = "CLAIM" | "ALL" | "PROMOTE";
type EntityTypeLocal = "schema" | "topic";

interface PromotionBannerProps {
  // `entityName` is only optional on
  // KlawApiModel<"PromotionStatus">
  // so we can't rely on it to be part of
  // the promotionDetails
  entityName: string;
  promotionDetails: PromotionStatus;
  type: EntityTypeLocal;
  promoteElement: ReactElement;
  hasOpenClaimRequest: boolean;
  hasOpenRequest: boolean;
  hasError: boolean;
  errorMessage: string;
}

function getRequestType({
  hasOpenClaimRequest,
  hasOpenPromotionRequest,
}: {
  hasOpenClaimRequest: boolean;
  hasOpenPromotionRequest: boolean;
}): RequestTypeLocal {
  if (hasOpenPromotionRequest) {
    return "PROMOTE";
  }
  if (hasOpenClaimRequest) {
    return "CLAIM";
  }
  return "ALL";
}

function createLink({
  type,
  entityName,
  requestType,
}: {
  type: EntityTypeLocal;
  entityName: string;
  requestType: RequestTypeLocal;
}): string {
  const table = requestType === "CLAIM" ? "approvals" : "requests";

  return `/${table}/${type}s?search=${entityName}&requestType=${requestType}&status=CREATED&page=1`;
}

function createText({
  type,
  entityName,
  requestType,
}: {
  type: "schema" | "topic";
  entityName: string;
  requestType: RequestTypeLocal;
}): string {
  const defaultText = `You cannot promote the ${type} at this time.`;

  if (requestType === "PROMOTE") {
    return `${defaultText} An promotion request for ${entityName} is already in progress.`;
  }
  if (requestType === "CLAIM") {
    return `${defaultText} A claim request for ${entityName} is in progress.`;
  }
  return `${defaultText} ${entityName} has a pending request.`;
}

const PromotionBanner = ({
  promotionDetails,
  hasOpenClaimRequest,
  hasOpenRequest,
  type,
  promoteElement,
  entityName,
  hasError,
  errorMessage,
}: PromotionBannerProps) => {
  const { status, sourceEnv, targetEnv, targetEnvId } = promotionDetails;

  if (hasError && errorMessage.length === 0) {
    console.error("Please pass a useful errorMessage for the user!");
  }
  // if any of these is not true,
  // the entity cannot be promoted
  const isPromotable =
    status !== "NOT_AUTHORIZED" &&
    status !== "NO_PROMOTION" &&
    status !== "FAILURE" &&
    targetEnv !== undefined &&
    sourceEnv !== undefined &&
    targetEnvId !== undefined &&
    entityName !== undefined;

  const hasOpenPromotionRequest = status === "REQUEST_OPEN";
  const hasPendingRequest =
    hasOpenRequest || hasOpenPromotionRequest || hasOpenClaimRequest;

  if (!isPromotable) return null;

  if (hasPendingRequest) {
    const requestType = getRequestType({
      hasOpenClaimRequest,
      hasOpenPromotionRequest,
    });
    const link = createLink({ type, entityName, requestType });
    const text = createText({ type, entityName, requestType });

    return (
      <Banner image={illustration} layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          {text}
        </Box>
        <InternalLinkButton to={link}>View request</InternalLinkButton>
      </Banner>
    );
  }

  return (
    <Banner image={illustration} layout="vertical" title={""}>
      <Spacing gap={"l1"}>
        {hasError && (
          <Alert type="error">
            {errorMessage.length > 0 ? errorMessage : "Unexpected error."}
          </Alert>
        )}

        <Box component={"p"} marginBottom={"l1"}>
          This {type} has not yet been promoted to the {targetEnv} environment.
        </Box>
      </Spacing>
      {promoteElement}
    </Banner>
  );
};

export { PromotionBanner };
