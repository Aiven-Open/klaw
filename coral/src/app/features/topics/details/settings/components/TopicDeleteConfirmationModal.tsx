import { Box, Checkbox, Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface TopicDeleteConfirmationModalProps {
  onClose: () => void;
  onSubmit: ({
    remark,
    deleteAssociatedSchema,
  }: {
    remark?: string;
    deleteAssociatedSchema: boolean;
  }) => void;
  isLoading: boolean;
}

const TopicDeleteConfirmationModal = ({
  onClose,
  onSubmit,
  isLoading,
}: TopicDeleteConfirmationModalProps) => {
  const [remark, setRemark] = useState<string | undefined>(undefined);
  const [deleteAssociatedSchema, setDeleteAssociatedSchema] = useState(false);

  return (
    <Modal
      title={"Request topic deletion"}
      close={onClose}
      primaryAction={{
        text: "Request topic deletion",
        onClick: () =>
          onSubmit({
            remark,
            deleteAssociatedSchema,
          }),
        loading: isLoading,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: onClose,
        disabled: isLoading,
      }}
    >
      <Box display={"flex"} flexDirection={"column"} rowGap={"l1"}>
        <p>
          Are you sure you want to proceed with the deletion request for this
          topic? Once the request is approved, the topic will be permanently
          deleted.
        </p>
        <>
          <Checkbox
            disabled={isLoading}
            checked={deleteAssociatedSchema}
            onChange={() => setDeleteAssociatedSchema(!deleteAssociatedSchema)}
          >
            Delete all versions of schema associated with this topic if a schema
            exists.
          </Checkbox>
        </>
        <Textarea
          labelText="Add your reasons for deleting this topic (optional)"
          placeholder="Write a message ..."
          onChange={(event) =>
            setRemark(event.target.value ? event.target.value : undefined)
          }
          valid={true}
          disabled={isLoading}
        />
      </Box>
    </Modal>
  );
};

export { TopicDeleteConfirmationModal };
