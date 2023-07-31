import { Alert, Banner, Box, Button, Spacing } from "@aivenio/aquarium";
import { Dispatch, SetStateAction } from "react";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";

interface TopicClaimBannerProps {
  topicName: string;
  hasOpenRequest: boolean;
  hasOpenClaimRequest: boolean;
  setShowClaimModal: Dispatch<SetStateAction<boolean>>;
  isError: boolean;
  errorMessage?: string;
}

const TopicClaimBanner = ({
  topicName,
  hasOpenClaimRequest,
  hasOpenRequest,
  setShowClaimModal,
  isError,
  errorMessage,
}: TopicClaimBannerProps) => {
  if (hasOpenRequest) {
    // We do not render an InternalLinkButton to the Requests page for this state...
    // .. because a user cannot see the requests opened by members of other teams
    return (
      <Banner layout="vertical" title={""}>
        <Box component={"p"}>
          There is an open request for {topicName} by the owners of this topic.
          Your team cannot claim ownership at this time.
        </Box>
      </Banner>
    );
  }

  if (hasOpenClaimRequest) {
    return (
      <Banner layout="vertical" title={""}>
        <Box component={"p"} marginBottom={"l1"}>
          There is already an open claim request for {topicName}.
        </Box>
        <InternalLinkButton
          to={`/requests/topics?search=${topicName}&requestType=CLAIM&status=CREATED&page=1`}
        >
          See the request
        </InternalLinkButton>
      </Banner>
    );
  }

  return (
    <Banner layout="vertical" title={""}>
      <Spacing gap={"l1"}>
        {isError && (
          <div role="alert">
            <Alert type="error">{errorMessage}</Alert>
          </div>
        )}
        <Box component={"p"} marginBottom={"l1"}>
          Your team is not the owner of this topic. Click below to create a
          claim request for this topic.
        </Box>
      </Spacing>
      <Button.Primary onClick={() => setShowClaimModal(true)}>
        Claim topic
      </Button.Primary>
    </Banner>
  );
};

export default TopicClaimBanner;
