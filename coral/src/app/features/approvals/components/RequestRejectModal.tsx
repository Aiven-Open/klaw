import { Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface RequestRejectModalProps {
  onClose: () => void;
  onCancel: () => void;
  onSubmit: (message: string) => void;
  isLoading: boolean;
}

const RequestRejectModal = ({
  onClose,
  onSubmit,
  onCancel,
  isLoading,
}: RequestRejectModalProps) => {
  const [rejectionMessage, setRejectionMessage] = useState("");
  return (
    <Modal
      title={"Reject request"}
      close={onClose}
      primaryAction={{
        text: "Reject request",
        onClick: () => onSubmit(rejectionMessage),
      }}
      secondaryAction={{ text: "Cancel", onClick: onCancel }}
      disabled={rejectionMessage === ""}
      isLoading={isLoading}
    >
      <Textarea
        labelText="Submit a reason to decline the request"
        name="rejection-reason"
        placeholder="Write a message ..."
        onChange={(e) => setRejectionMessage(e.target.value)}
        required
      />
    </Modal>
  );
};

export default RequestRejectModal;
