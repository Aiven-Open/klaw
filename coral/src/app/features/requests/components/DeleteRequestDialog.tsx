import { Dialog } from "src/app/components/Dialog";

type DeleteRequestModalProps = {
  deleteRequest: () => void;
  cancel: () => void;
  // 'isLoading' will be required later. It is added as optional
  // for now, since we're still aligning on the approach
  // and, we don't want to break other component using this.
  isLoading?: boolean;
};

function DeleteRequestDialog({
  cancel,
  deleteRequest,
  isLoading,
}: DeleteRequestModalProps) {
  return (
    <Dialog
      title={"Delete request"}
      primaryAction={{
        text: "Delete request",
        onClick: deleteRequest,
        loading: isLoading,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: cancel,
        disabled: isLoading,
      }}
      type={"danger"}
    >
      Are you sure you want to delete the request?
    </Dialog>
  );
}

export { DeleteRequestDialog };
