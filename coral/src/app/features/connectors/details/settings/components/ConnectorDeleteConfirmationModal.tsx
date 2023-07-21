import { Box, Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface ConnectorDeleteConfirmationModalProps {
  onClose: () => void;
  onSubmit: (remark?: string) => void;
  isLoading: boolean;
}

const ConnectorDeleteConfirmationModal = ({
  onClose,
  onSubmit,
  isLoading,
}: ConnectorDeleteConfirmationModalProps) => {
  const [remark, setRemark] = useState<string | undefined>(undefined);

  return (
    <Modal
      title={"Delete connector"}
      close={onClose}
      primaryAction={{
        text: "Request connector deletion",
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
        <p>
          Are you sure you would like to request the deletion of this connector?
        </p>
        <Textarea
          labelText="You can add the reason to delete the connector (optional)"
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

export { ConnectorDeleteConfirmationModal };
