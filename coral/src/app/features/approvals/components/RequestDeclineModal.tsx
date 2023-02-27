import { Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface RequestDeclineModalProps {
  onClose: () => void;
  onCancel: () => void;
  onSubmit: (message: string) => void;
  isLoading: boolean;
}

const RequestDeclineModal = ({
  onClose,
  onSubmit,
  onCancel,
  isLoading,
}: RequestDeclineModalProps) => {
  const [declineReason, setDeclineReason] = useState("");
  const isValid = declineReason.length < 300;

  return (
    <Modal
      title={"Decline request"}
      close={onClose}
      primaryAction={{
        text: "Decline request",
        onClick: () => onSubmit(declineReason),
        disabled: !isValid || declineReason.length === 0,
        loading: isLoading,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: onCancel,
        disabled: isLoading,
      }}
    >
      <Textarea
        labelText="Submit a reason to decline the request"
        name="decline-reason"
        placeholder="Write a message ..."
        onChange={(e) => setDeclineReason(e.target.value)}
        helperText={!isValid ? "Reason to decline is too long." : undefined}
        valid={isValid}
        disabled={isLoading}
        required
      />
    </Modal>
  );
};

export default RequestDeclineModal;
