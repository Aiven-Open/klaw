import { Banner, Box, Button, GridItem } from "@aivenio/aquarium";
import illustration from "src/app/images/topic-details-schema-Illustration.svg";
import { ReactElement } from "react";
import { KlawApiModel } from "types/utils";

interface PromotionBannerProps {
  // `topicName` is only optional on
  // KlawApiModel<"PromotionStatus">
  // so we can't rely on it to be part of
  // the promotionDetails
  topicName: string;
  promotionDetails: KlawApiModel<"PromotionStatus">;
  type: "schema" | "topic";
  promoteElement: ReactElement;
  hasOpenRequest: boolean;
}

const PromotionBanner = ({
  promotionDetails,
  hasOpenRequest,
  type,
  promoteElement,
  topicName,
}: PromotionBannerProps) => {
  const { status, sourceEnv, targetEnv, targetEnvId } = promotionDetails;

  // if any of these is not true,
  // the entity cannot be promoted
  const isPromotable =
    status !== "NOT_AUTHORIZED" &&
    status !== "NO_PROMOTION" &&
    status !== "FAILURE" &&
    targetEnv !== undefined &&
    sourceEnv !== undefined &&
    targetEnvId !== undefined &&
    topicName !== undefined;

  const hasOpenPromotionRequest = status === "REQUEST_OPEN";

  if (!isPromotable) return null;

  if (hasOpenRequest) {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box component={"p"} marginBottom={"l1"}>
            There is an open {type} request for {topicName}.
          </Box>
          <Button.ExternalLink
            href={`/requests/${type}s?search=${topicName}&status=CREATED&page=1`}
          >
            See the request
          </Button.ExternalLink>
        </Banner>
      </GridItem>
    );
  }

  if (hasOpenPromotionRequest) {
    return (
      <GridItem colSpan={"span-2"}>
        <Banner image={illustration} layout="vertical" title={""}>
          <Box component={"p"} marginBottom={"l1"}>
            There is already an open promotion request for {topicName}.
          </Box>
          <Button.ExternalLink
            href={`/requests/${type}s?search=${topicName}&requestType=PROMOTE&status=CREATED&page=1`}
          >
            See the request
          </Button.ExternalLink>
        </Banner>
      </GridItem>
    );
  }

  return (
    <GridItem colSpan={"span-2"}>
      <Banner image={illustration} layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          This {type} has not yet been promoted to the {targetEnv} environment.
        </Box>
        {promoteElement}
      </Banner>
    </GridItem>
  );
};

export { PromotionBanner };
