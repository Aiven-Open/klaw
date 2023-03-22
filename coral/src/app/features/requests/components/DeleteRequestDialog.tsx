import { Dialog } from "src/app/components/Dialog";

type DeleteRequestModalProps = {
  deleteRequest: () => void;
  cancel: () => void;
  // @TODO will be required later, will add it as optional
  // for now, since we're still aligning on the approach
  // and I don't want to break other component using this once
  // if we want to do it that way, I'll update the prop in a
  // separate PR
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
