import { Dialog } from "@aivenio/aquarium";

type CancelRequestDialogProps = {
  open: boolean;
  title: string;
  onConfirm: () => void;
  onDismiss: () => void;
};

function CancelRequestDialog(props: CancelRequestDialogProps) {
  const { open, title, onConfirm, onDismiss } = props;

  return (
    <Dialog
      type="warning"
      title={title}
      open={open}
      primaryAction={{ text: "Cancel request", onClick: onConfirm }}
      secondaryAction={{ text: "Continue with request", onClick: onDismiss }}
    >
      Do you want to cancel this request? The data added will be lost.
    </Dialog>
  );
}

export { CancelRequestDialog };
