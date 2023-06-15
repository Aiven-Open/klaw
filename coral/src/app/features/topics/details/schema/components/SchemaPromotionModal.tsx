import { Box, Switch, Textarea } from "@aivenio/aquarium";
import { useState } from "react";
import { Modal } from "src/app/components/Modal";

interface SchemaPromotionModalProps {
  onClose: () => void;
  onSubmit: ({
    remarks,
    forceRegister,
  }: {
    remarks: string;
    forceRegister: boolean;
  }) => void;
  isLoading: boolean;
  targetEnvironment?: string;
  version: number;
  showForceRegister: boolean;
}

const SchemaPromotionModal = ({
  onClose,
  onSubmit,
  isLoading,
  targetEnvironment,
  version,
  showForceRegister,
}: SchemaPromotionModalProps) => {
  const [remarks, setRemarks] = useState<string>("");
  const [forceRegister, setForceRegister] = useState(false);

  return (
    <Modal
      title={`Promote schema to ${targetEnvironment}`}
      close={onClose}
      primaryAction={{
        text: "Request schema promotion",
        onClick: () =>
          onSubmit({
            remarks,
            forceRegister,
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
          {`Promote the Version ${version} of the schema to ${targetEnvironment}?`}
        </p>
        {showForceRegister && (
          <Switch
            disabled={isLoading}
            checked={forceRegister}
            caption={
              "Overrides some validation that the schema registry would normally do."
            }
            onChange={() => setForceRegister(!forceRegister)}
          >
            Force register
          </Switch>
        )}
        <Textarea
          labelText="You can add the reason to promote the schema (optional)"
          placeholder="Write a message..."
          onChange={(event) =>
            setRemarks(event.target.value ? event.target.value : "")
          }
          value={remarks}
          helperText={"Required"}
          disabled={isLoading}
        />
      </Box>
    </Modal>
  );
};

export { SchemaPromotionModal };
