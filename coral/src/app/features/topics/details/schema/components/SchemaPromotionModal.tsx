import { Alert, Box, Checkbox, Textarea } from "@aivenio/aquarium";
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
        text: showForceRegister ? "Force register" : "Request schema promotion",
        onClick: () =>
          onSubmit({
            remarks,
            forceRegister,
          }),
        loading: isLoading,
        disabled: showForceRegister && !forceRegister,
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
        {showForceRegister && (
          <>
            <Box>
              <Alert type={"warning"}>Uploaded schema appears invalid.</Alert>
            </Box>

            <Checkbox
              disabled={isLoading}
              checked={forceRegister}
              caption={
                <>
                  Warning: This will override standard validation process of the
                  schema registry.{" "}
                  <a
                    href={
                      "https://www.klaw-project.io/docs/HowTo/schemas/Promote-a-schema/#how-does-force-register-work"
                    }
                  >
                    Learn more
                  </a>
                </>
              }
              onChange={(e) => setForceRegister(e.target.checked)}
            >
              Force register schema promotion
            </Checkbox>
          </>
        )}
      </Box>
    </Modal>
  );
};

export { SchemaPromotionModal };
