import { Dialog } from "src/app/components/Dialog";

type DeleteRequestModalProps = {
  deleteRequest: () => void;
  cancel: () => void;
};

function DeleteRequestDialog({
  cancel,
  deleteRequest,
}: DeleteRequestModalProps) {
  return (
    <Dialog
      title={"Delete request"}
      primaryAction={{
        text: "Delete request",
        onClick: deleteRequest,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: cancel,
      }}
      type={"danger"}
    >
      Are you sure you want to delete the request?
    </Dialog>
  );
}

export { DeleteRequestDialog };
