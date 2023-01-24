import { Controller as _Controller, useFormContext } from "react-hook-form";
import { useEffect, useState } from "react";
import { BorderBox, Box, Typography } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { editor } from "monaco-editor";
import z, { ZodSchema } from "zod";
import { readFile } from "src/app/features/topics/schema-request/utils/read-file";

type TopicSchemaProps = {
  name: string;
  file: File | undefined;
  formSchema: ZodSchema;
};

function TopicSchema(props: TopicSchemaProps) {
  const { name, file, formSchema } = props;

  const { setValue, setError, control } =
    useFormContext<z.infer<typeof formSchema>>();
  const [schema, setSchema] = useState<string>("");

  useEffect(() => {
    if (file) {
      // eslint-disable-next-line no-inner-declarations
      async function getFile() {
        return await readFile(file);
      }
      getFile().then((fileContent) => {
        setSchema(fileContent);
      });
    }
  }, [file]);

  function validateSchema(markers: editor.IMarker[]) {
    if (markers.length > 0) {
      setError(name, { message: markers[0].message, type: "custom" });
    } else {
      setValue(name, schema, {
        shouldValidate: true,
        shouldDirty: true,
        shouldTouch: true,
      });
    }
  }

  return (
    <_Controller
      name={name}
      control={control}
      render={({ fieldState: { error } }) => {
        return (
          <div>
            <Typography.Caption fontWeight={"500"} htmlTag={"label"}>
              <span className={error?.message && "text-error-50"}>
                Schema preview (read only)
              </span>
            </Typography.Caption>
            <BorderBox
              borderColor={error?.message ? "error-50" : "grey-20"}
              marginTop={"1"}
            >
              {!schema ? (
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
              ) : (
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
              )}
            </BorderBox>
            <Box
              component={"p"}
              marginTop={"1"}
              marginBottom={"3"}
              className={"text-error-50 typography-caption-default"}
            >
              {error?.message ? `${error?.message}` : <>&nbsp;</>}
            </Box>
          </div>
        );
      }}
    />
  );
}

export { TopicSchema, readFile };
