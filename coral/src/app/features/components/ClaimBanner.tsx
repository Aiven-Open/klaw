import { Alert, Banner, Box, Button, Spacing } from "@aivenio/aquarium";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import illustration from "src/app/images/topic-details-banner-Illustration.svg";

interface ClaimBannerProps {
  entityType: "topic" | "connector";
  entityName: string;
  hasOpenRequest: boolean;
  hasOpenClaimRequest: boolean;
  claimEntity: () => void;
  isError: boolean;
  errorMessage?: string;
  entityOwner: string;
}

const ClaimBanner = ({
  entityType,
  entityName,
  hasOpenClaimRequest,
  hasOpenRequest,
  claimEntity,
  isError,
  errorMessage,
  entityOwner,
}: ClaimBannerProps) => {
  // if there is an open claim request, hasOpenRequest is true, too
  if (hasOpenRequest && !hasOpenClaimRequest) {
    // We do not render an InternalLinkButton to the Requests page for this state...
    // .. because a user cannot see the requests opened by members of other teams
    return (
      <Banner image={illustration} layout="vertical" title={""}>
        <Box.Flex minHeight={"full"}>
          <Box.Flex component={"p"} alignSelf={"center"}>
            {`Your team cannot claim ownership at this time. ${entityName} has pending requests.`}
          </Box.Flex>
        </Box.Flex>
      </Banner>
    );
  }

  if (hasOpenClaimRequest) {
    return (
      <Banner image={illustration} layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          Your team cannot claim ownership at this time. A claim request for{" "}
          {entityName} is already in progress.
        </Box>
        <InternalLinkButton
          to={`/requests/${entityType}s?search=${entityName}&requestType=CLAIM&status=CREATED&page=1`}
        >
          View request
        </InternalLinkButton>
      </Banner>
    );
  }

  return (
    <Banner image={illustration} layout="vertical" title={""}>
      <Spacing gap={"l1"}>
        {isError && <Alert type="error">{errorMessage}</Alert>}
        <Box component={"p"} marginBottom={"l1"}>
          {`This ${entityType} is currently owned by ${entityOwner}. Select "Claim ${entityType}" to request ownership.`}
        </Box>
      </Spacing>
      <Button.Primary onClick={claimEntity}>Claim {entityType}</Button.Primary>
    </Banner>
  );
};

export { ClaimBanner };
