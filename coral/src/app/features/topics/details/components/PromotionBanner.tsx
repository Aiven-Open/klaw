import { Alert, Banner, Box, Spacing } from "@aivenio/aquarium";
import { ReactElement } from "react";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import illustration from "src/app/images/topic-details-schema-Illustration.svg";
import { PromotionStatus } from "src/domain/promotion";
import { RequestOperationType } from "src/domain/requests/requests-types";

interface PromotionBannerProps {
  // `entityName` is only optional on
  // KlawApiModel<"PromotionStatus">
  // so we can't rely on it to be part of
  // the promotionDetails
  entityName: string;
  promotionDetails: PromotionStatus;
  type: "schema" | "topic";
  promoteElement: ReactElement;
  hasOpenRequest: boolean;
  hasError: boolean;
  errorMessage: string;
}

const PromotionBanner = ({
  promotionDetails,
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

  if (!isPromotable) return null;

  if (hasOpenRequest) {
    return (
      <Banner image={illustration} layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          There is an open {type} request for {entityName}.
        </Box>
        <InternalLinkButton
          to={`/requests/${type}s?search=${entityName}&status=CREATED&page=1`}
        >
          See the request
        </InternalLinkButton>
      </Banner>
    );
  }

  if (hasOpenPromotionRequest) {
    // Schema currently shows all types as "CREATE"
    const requestType: RequestOperationType =
      type === "topic" ? "PROMOTE" : "CREATE";
    return (
      <Banner image={illustration} layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          There is already an open promotion request for {entityName}.
        </Box>
        <InternalLinkButton
          to={`/requests/${type}s?search=${entityName}&requestType=${requestType}&status=CREATED&page=1`}
        >
          See the request
        </InternalLinkButton>
      </Banner>
    );
  }

  return (
    <Banner image={illustration} layout="vertical" title={""}>
      <Spacing gap={"l1"}>
        {hasError && (
          <div role="alert">
            <Alert type="error">
              {errorMessage.length > 0 ? errorMessage : "Unexpected error."}
            </Alert>
          </div>
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
