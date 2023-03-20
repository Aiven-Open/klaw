import { Modal } from "src/app/components/Modal";

type DeleteRequestModalProps = {
  deleteRequest: () => void;
  close: () => void;
};

function DeleteRequestModal({ close, deleteRequest }: DeleteRequestModalProps) {
  return (
    <Modal
      title={"Delete request"}
      primaryAction={{
        text: "Delete request",
        onClick: deleteRequest,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: close,
      }}
      close={close}
    >
      <>Are you sure you want to delete the request?</>
    </Modal>
  );
}

export { DeleteRequestModal };
