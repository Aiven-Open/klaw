import { Box, Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface ClaimConfirmationModalProps {
  entity: "topic" | "connector";
  onClose: () => void;
  onSubmit: (remark?: string) => void;
  isLoading: boolean;
}

const ClaimConfirmationModal = ({
  onClose,
  onSubmit,
  isLoading,
  entity,
}: ClaimConfirmationModalProps) => {
  const [remark, setRemark] = useState<string | undefined>(undefined);

  return (
    <Modal
      title={`Claim ${entity}`}
      close={onClose}
      primaryAction={{
        text: `Request claim ${entity}`,
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
        <p>{`Are you sure you would like to claim this ${entity}?`}</p>
        <Textarea
          labelText={`You can add the reason to claim the ${entity} (optional)`}
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

export { ClaimConfirmationModal };
