import { Box, Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface TopicClaimConfirmationModalProps {
  onClose: () => void;
  onSubmit: (remark?: string) => void;
  isLoading: boolean;
}

const TopicClaimConfirmationModal = ({
  onClose,
  onSubmit,
  isLoading,
}: TopicClaimConfirmationModalProps) => {
  const [remark, setRemark] = useState<string | undefined>(undefined);

  return (
    <Modal
      title={"Claim topic"}
      close={onClose}
      primaryAction={{
        text: "Request claim topic",
        onClick: () => onSubmit(remark),
        loading: isLoading,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: onClose,
        disabled: isLoading,
      }}
    >
      <Box.Flex flexDirection={"column"} rowGap={"l1"}>
        <p>Are you sure you would like to claim this topic?</p>
        <Textarea
          labelText="You can add the reason to claim the topic (optional)"
          placeholder="Write a message ..."
          onChange={(event) =>
            setRemark(event.target.value ? event.target.value : undefined)
          }
          valid={true}
          disabled={isLoading}
        />
      </Box.Flex>
    </Modal>
  );
};

export { TopicClaimConfirmationModal };
