import { ReactElement } from "react";
import { Modal } from "src/app/components/Modal";

interface RequestDetailsModalProps {
  onClose: () => void;
  onApprove: () => void;
  onDecline: () => void;
  children: ReactElement;
  isLoading: boolean;
  disabledActions?: boolean;
}

const RequestDetailsModal = ({
  children,
  onClose,
  onApprove,
  onDecline,
  isLoading,
  disabledActions,
}: RequestDetailsModalProps) => {
  return (
    <Modal
      title={"Request details"}
      close={onClose}
      primaryAction={{
        text: "Approve",
        onClick: onApprove,
        loading: isLoading,
        disabled: disabledActions,
      }}
      secondaryAction={{
        text: "Decline",
        onClick: onDecline,
        disabled: isLoading || disabledActions,
      }}
    >
      {children}
    </Modal>
  );
};

export default RequestDetailsModal;
