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
  const [remarks, setRemarks] = useState<string>(
    `Promote schema to ${targetEnvironment}`
  );
  const [forceRegister, setForceRegister] = useState(false);

  return (
    <Modal
      title={`Promote schema to ${targetEnvironment}`}
      close={onClose}
      primaryAction={{
        text: "Promote schema",
        onClick: () =>
          onSubmit({
            remarks,
            forceRegister,
          }),
        loading: isLoading,
        disabled: remarks.length === 0,
      }}
      secondaryAction={{
        text: "Cancel",
        onClick: onClose,
        disabled: isLoading,
      }}
    >
      <Box display={"flex"} flexDirection={"column"} rowGap={"l1"}>
        <p>
          Promote the Version {version} of the schema to {targetEnvironment}?
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
          labelText="Add the reason to promote the schema"
          placeholder="Write a message ..."
          onChange={(event) =>
            setRemarks(event.target.value ? event.target.value : "")
          }
          value={remarks}
          valid={remarks.length > 0}
          helperText={"Required"}
          disabled={isLoading}
          required
        />
      </Box>
    </Modal>
  );
};

export { SchemaPromotionModal };
