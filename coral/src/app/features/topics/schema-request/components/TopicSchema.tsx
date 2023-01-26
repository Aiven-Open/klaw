import { Controller as _Controller, useFormContext } from "react-hook-form";
import { ChangeEvent, useEffect, useState } from "react";
import { BorderBox, Box, Typography } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { editor } from "monaco-editor";
import { readFile } from "src/app/features/topics/schema-request/utils/read-file";
import { TopicRequestFormSchema } from "src/app/features/topics/schema-request/utils/zod-schema";
import { FileInput } from "src/app/components/FileInput";
import isString from "lodash/isString";

type TopicSchemaProps = {
  name: keyof TopicRequestFormSchema;
  required: boolean;
};

function TopicSchema(props: TopicSchemaProps) {
  const { name, required } = props;
  const { setValue, setError, clearErrors, control } =
    useFormContext<TopicRequestFormSchema>();

  const [schema, setSchema] = useState<string | undefined>(undefined);

  useEffect(() => {
    if (isString(schema)) {
      setValue(name, schema, {
        shouldValidate: true,
        shouldTouch: true,
        shouldDirty: true,
      });
    }
  }, [schema]);

  function checkEmptyFile(event: ChangeEvent<HTMLInputElement>) {
    if (!required) return;
    const file = event.target?.files?.[0];
    if (!file) {
      setError(name, { message: "File is a required field" });
    }
  }

  function validateSchema(markers: editor.IMarker[]) {
    if (markers.length > 0) {
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
        clearErrors();
        setSchema(fileContent);
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
              {...props}
              buttonText={"Upload AVRO schema"}
              labelText={"Upload AVRO schema file"}
              noFileText={"No file chosen"}
              helperText={error?.message || ""}
              required={required}
              onBlur={checkEmptyFile}
              onChange={uploadFile}
              valid={!error}
            />
            <Typography.Caption fontWeight={"500"} htmlTag={"label"}>
              <span className={error?.message && "text-error-50"}>
                Schema preview (read only)
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
              <div style={{ display: `${!schema ? "none" : "block"}` }}>
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
                    scrollbar: {
                      vertical: "hidden",
                    },
                  }}
                />
              </div>
            </BorderBox>
            <Box
              component={"p"}
              marginTop={"1"}
              marginBottom={"3"}
              className={"text-error-50 typography-caption-default"}
              aria-hidden={"true"}
            >
              {error?.message ? `${error?.message}` : <>&nbsp;</>}
            </Box>
          </div>
        );
      }}
    />
  );
}

export { TopicSchema };
