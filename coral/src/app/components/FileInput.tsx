import { Icon, Box } from "@aivenio/aquarium";
import { omit, uniqueId } from "lodash";
import { InputHTMLAttributes, useRef } from "react";
import classes from "src/app/components/FileInput.module.css";
import cloudUpload from "@aivenio/aquarium/dist/src/icons/cloudUpload";

type FileInputProps = InputHTMLAttributes<HTMLInputElement> & {
  valid: boolean;
  labelText: string;
  helperText: string;
  noFileText: string;
  fileName?: string;
};

function FileInput(props: FileInputProps) {
  const { valid, labelText, helperText, noFileText, fileName } = props;

  const inputRef = useRef<null | HTMLInputElement>(null);
  const inputId = uniqueId("file_upload_");
  const errorMessageId = uniqueId("file_upload_error_message");

  const propsPassed = omit(props, [
    "valid",
    "labelText",
    "helperText",
    "noFileText",
    "fileName",
  ]);

  function handleWrapperClick() {
    inputRef?.current?.click();
  }

  return (
    <div className={classes.wrapper}>
      <div
        className={classes.overlay}
        aria-hidden={"true"}
        onClick={handleWrapperClick}
      >
        <Box display={"flex"} alignItems={"center"}>
          <Box
            component={"div"}
            display={"flex"}
            alignItems={"center"}
            colGap={"2"}
            borderWidth={"1px"}
            borderColor={"grey-30"}
            borderRadius={"2px"}
            paddingX={"l1"}
            paddingY={"3"}
          >
            <Icon icon={cloudUpload} />
            <span>{labelText}</span>
          </Box>
          <Box
            grow={"1"}
            borderWidth={"1px"}
            borderRadius={"2px"}
            borderColor={"grey-20"}
            backgroundColor={"grey-5"}
            paddingX={"l1"}
            paddingY={"3"}
            marginLeft={"l1"}
          >
            {fileName ? fileName : noFileText}
          </Box>
        </Box>
      </div>
      <div className={classes.fileInputWrapper}>
        <Box display={"flex"} flexDirection={"column"} colGap={"1"}>
          <label
            htmlFor={inputId}
            className={`${classes.fileInputLabel} ${
              !valid && "border-error-50"
            }`}
          >
            {labelText}
            <input
              {...propsPassed}
              id={inputId}
              type={"file"}
              ref={inputRef}
              aria-invalid={`${!valid}`}
              className={classes.fileInput}
              {...(!valid && { "aria-describedby": errorMessageId })}
            />
          </label>
          <Box
            component={"p"}
            marginTop={"1"}
            marginBottom={"3"}
            className={"text-error-50 typography-caption-default"}
          >
            {valid ? <>&nbsp;</> : <>{helperText}</>}
          </Box>
        </Box>
      </div>
    </div>
  );
}

export { FileInput };
export type { FileInputProps };
