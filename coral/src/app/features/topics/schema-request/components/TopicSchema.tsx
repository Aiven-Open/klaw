import { Controller as _Controller, useFormContext } from "react-hook-form";
import { ChangeEvent, useEffect, useState } from "react";
import { BorderBox, Box, Typography } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { editor } from "monaco-editor";
import { readFile } from "src/app/features/topics/schema-request/utils/read-file";
import { TopicRequestFormSchema } from "src/app/features/topics/schema-request/form-schemas/topic-schema-request-form";
import { FileInput } from "src/app/components/FileInput";
import { SchemaRequest } from "src/domain/schema-request";

type TopicSchemaProps = {
  name: keyof TopicRequestFormSchema;
  schemaType: SchemaRequest["schemaType"];
  required: boolean;
};

function TopicSchema(props: TopicSchemaProps) {
  const { name, required, schemaType = "AVRO" } = props;
  const [fileName, setFileName] = useState<string | undefined>(undefined);

  const { setValue, setError, clearErrors, control } =
    useFormContext<TopicRequestFormSchema>();

  const [schema, setSchema] = useState<string | undefined>(undefined);

  useEffect(() => {
    if (schema) {
      setValue(name, schema, {
        shouldValidate: true,
        shouldTouch: true,
        shouldDirty: true,
      });
    }
  }, [schema]);

  // Reset value of schema when switching between schema types
  useEffect(() => {
    if (schema) {
      setFileName(undefined);
      setSchema(undefined);
      setValue(name, undefined, {
        shouldValidate: false,
        shouldTouch: false,
        shouldDirty: false,
      });
    }
  }, [schemaType]);

  function checkEmptyFile(event: ChangeEvent<HTMLInputElement>) {
    if (!required) return;
    const file = event.target?.files?.[0];
    if (!file) {
      setError(name, {
        message: `File missing: Upload the ${schemaType} schema file.`,
      });
    }
  }

  function validateSchema(markers: editor.IMarker[]) {
    if (markers.length > 0) {
      // We need to ignore this error because when uploading a JSON schema with a $schema property monaco-editor will error with:
      // Unable to load schema from {value from $schema}. No schema request service available
      // After some investigation, no obvious solution was apparent.
      // As this error has nothing to do with what Klaw is doing, or the user's schema, we can probably ignore it.
      const isSchemaRequestServiceError = markers?.[0]?.message.includes(
        "No schema request service available"
      );

      if (isSchemaRequestServiceError) {
        return;
      }

      setError(name, { message: markers?.[0]?.message, type: "custom" });
      setValue(name, undefined, {
        shouldValidate: false,
        shouldDirty: true,
        shouldTouch: true,
      });
    }
  }

  function uploadFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target?.files?.[0];
    if (file) {
      readFile(file).then((fileContent) => {
        // @Todo add better error handling in case of
        // empty file as an improvement
        if (fileContent) {
          clearErrors();
          setSchema(fileContent);
          setFileName(file.name);
        } else {
          setError(name, {
            message: "Uploaded file is empty, please chose a different one.",
          });
          setSchema("");
        }
      });
    } else {
      setError(name, { message: "File is a required field" });
      setSchema(undefined);
    }
  }

  return (
    <_Controller
      name={name}
      control={control}
      render={({ fieldState: { error } }) => {
        return (
          <div>
            <FileInput
              name={name}
              buttonText={`Upload ${schemaType} schema`}
              labelText={`Upload ${schemaType} schema file`}
              helperText={error?.message || ""}
              required={required}
              onBlur={checkEmptyFile}
              onChange={uploadFile}
              fileName={fileName}
              valid={!error}
            />
            <Typography.Caption htmlTag={"label"}>
              <span className={error?.message && "text-error-50"}>
                Schema preview (read-only)
              </span>
            </Typography.Caption>
            <BorderBox
              borderColor={error?.message ? "error-50" : "grey-20"}
              marginTop={"1"}
            >
              {!schema && (
                <div style={{ height: "200px" }}>
                  <Box
                    display={"flex"}
                    justifyContent={"center"}
                    alignItems={"center"}
                    height={"full"}
                    backgroundColor={"grey-5"}
                    aria-hidden={"true"}
                  >
                    Preview for your schema
                  </Box>
                </div>
              )}
              {schema && (
                <MonacoEditor
                  data-testid="topic-schema"
                  height="200px"
                  language="json"
                  theme={"light"}
                  value={schema}
                  onValidate={validateSchema}
                  loading={"Loading preview"}
                  options={{
                    ariaLabel: "Preview",
                    readOnly: true,
                    domReadOnly: true,
                    renderControlCharacters: false,
                    minimap: { enabled: false },
                    folding: false,
                    lineNumbers: "off",
                    scrollBeyondLastLine: false,
                  }}
                />
              )}
            </BorderBox>
            <Box marginTop={"1"} marginBottom={"3"} aria-hidden={"true"}>
              <Typography.Caption color={"error-50"}>
                {error?.message ? `${error?.message}` : <>&nbsp;</>}
              </Typography.Caption>
            </Box>
          </div>
        );
      }}
    />
  );
}

export { TopicSchema };
