import { ReactElement } from "react";
import { Modal } from "src/app/components/Modal";

type RequestDetailsModalActions = {
  primary: {
    text: string;
    onClick: () => void;
  };
  secondary: {
    text: string;
    onClick: () => void;
    disabled?: boolean;
  };
};

interface RequestDetailsModalProps {
  onClose: () => void;
  actions: RequestDetailsModalActions;
  children: ReactElement;
  isLoading: boolean;
  disabledActions?: boolean;
}

const RequestDetailsModal = ({
  children,
  onClose,
  actions,
  isLoading,
  disabledActions,
}: RequestDetailsModalProps) => {
  return (
    <Modal
      title={"Request details"}
      close={onClose}
      primaryAction={{
        ...actions.primary,
        loading: isLoading,
        disabled: disabledActions,
      }}
      secondaryAction={{
        ...actions.secondary,
        disabled: actions.secondary.disabled || isLoading || disabledActions,
      }}
    >
      {children}
    </Modal>
  );
};

export default RequestDetailsModal;
