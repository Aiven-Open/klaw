import { ReactElement } from "react";
import { Modal } from "src/app/components/Modal";

interface RequestDetailsModalProps {
  onClose: () => void;
  onApprove: () => void;
  onReject: () => void;
  children: ReactElement;
  isLoading: boolean;
}

const RequestDetailsModal = ({
  children,
  onClose,
  onApprove,
  onReject,
  isLoading,
}: RequestDetailsModalProps) => {
  return (
    <Modal
      title={"Request details"}
      close={onClose}
      primaryAction={{
        text: "Approve",
        onClick: onApprove,
        loading: isLoading,
      }}
      secondaryAction={{
        text: "Reject",
        onClick: onReject,
        disabled: isLoading,
      }}
    >
      {children}
    </Modal>
  );
};

export default RequestDetailsModal;
