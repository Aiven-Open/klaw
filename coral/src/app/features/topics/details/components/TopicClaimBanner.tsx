import {
  Alert,
  Banner,
  Box,
  Button,
  GridItem,
  Spacing,
} from "@aivenio/aquarium";
import { Dispatch, SetStateAction } from "react";

interface TopicClaimBannerProps {
  setShowClaimModal: Dispatch<SetStateAction<boolean>>;
  isError: boolean;
  errorMessage?: string;
}

const TopicClaimBanner = ({
  setShowClaimModal,
  isError,
  errorMessage,
}: TopicClaimBannerProps) => {
  return (
    <GridItem colSpan={"span-2"} marginBottom={"l1"}>
      <Banner layout="vertical" title={""}>
        <Spacing gap={"l1"}>
          {isError && (
            <div role="alert">
              <Alert type="error">{errorMessage}</Alert>
            </div>
          )}
          <Box.Flex component={"p"} marginBottom={"l1"}>
            Your team is not the owner of this topic. Click below to create a
            claim request for this topic.
          </Box.Flex>
        </Spacing>
        <Button.Primary onClick={() => setShowClaimModal(true)}>
          Claim topic
        </Button.Primary>
      </Banner>
    </GridItem>
  );
};

export default TopicClaimBanner;
