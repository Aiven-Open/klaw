import { Icon, Box, Typography, Grid, GridItem } from "@aivenio/aquarium";
import omit from "lodash/omit";
import uniqueId from "lodash/uniqueId";
import { InputHTMLAttributes, useRef } from "react";
import classes from "src/app/components/FileInput.module.css";
import cloudUpload from "@aivenio/aquarium/dist/src/icons/cloudUpload";

type FileInputProps = InputHTMLAttributes<HTMLInputElement> & {
  valid: boolean;
  // labelText is where the important information for
  // visual and AT users is transported!
  labelText: string;
  // buttonText is not conveyed to e.g. screen reader
  // users, treat is as more decorative text
  buttonText: string;
  helperText: string;
  noFileText: string;
};

function FileInput(props: FileInputProps) {
  const { valid, labelText, buttonText, helperText, noFileText } = props;

  const inputRef = useRef<null | HTMLInputElement>(null);
  const inputId = uniqueId("file_upload_");
  const errorMessageId = uniqueId("file_upload_error_message");
  const currentFileName = inputRef.current?.files?.[0]?.name;

  const inputAttributes = omit(props, [
    "valid",
    "labelText",
    "buttonText",
    "helperText",
    "noFileText",
    "fileName",
  ]);

  function handleWrapperClick() {
    inputRef?.current?.click();
  }

  return (
    <div>
      <Box
        aria-hidden={true}
        marginBottom={"2"}
        data-testid="file-input-fake-label"
      >
        <Typography.Caption fontWeight={"500"}>
          {labelText}
          {props.required && <span className={"text-error-50"}>*</span>}
        </Typography.Caption>
      </Box>
      <Grid
        colGap={"l1"}
        cols={"2"}
        rows={"1"}
        style={{
          gridTemplateColumns: "max-content auto",
        }}
      >
        <GridItem colStart={"1"} colEnd={"1"} rowStart={"1"} rowEnd={"1"}>
          <Box
            aria-hidden={true}
            display={"flex"}
            alignItems={"center"}
            backgroundColor={"white"}
            className={`${classes.fakeButton}`}
            onClick={handleWrapperClick}
            data-testid="file-input-fake-button"
          >
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
              minWidth={"full"}
              className={`${!valid && "border-error-50"}`}
            >
              <Icon icon={cloudUpload} />
              <span>{buttonText}</span>
            </Box>
          </Box>
        </GridItem>

        <GridItem colStart={"2"} colEnd={"2"} rowStart={"1"} rowEnd={"1"}>
          <Box
            grow={"1"}
            borderWidth={"1px"}
            borderRadius={"2px"}
            borderColor={"grey-20"}
            backgroundColor={"grey-5"}
            paddingX={"l1"}
            paddingY={"3"}
            aria-hidden={true}
            data-testid="file-input-filename-info"
          >
            {currentFileName || noFileText}
          </Box>
        </GridItem>

        <GridItem
          className={classes.fileInputWrapper}
          colStart={"1"}
          colEnd={"1"}
          rowStart={"1"}
          rowEnd={"1"}
        >
          <label htmlFor={inputId}>
            <span className={"visually-hidden"}>
              {currentFileName
                ? `Uploaded file, name: ${inputRef.current?.files?.[0]?.name}. Click to upload new file.`
                : labelText}
            </span>
            <input
              {...inputAttributes}
              id={inputId}
              type={"file"}
              ref={inputRef}
              aria-required={props.required}
              aria-invalid={`${!valid}`}
              className={`${classes.fileInput}`}
              {...(!valid && { "aria-describedby": errorMessageId })}
            />
          </label>
        </GridItem>
        <Box
          component={"p"}
          id={errorMessageId}
          marginTop={"1"}
          marginBottom={"3"}
          className={"text-error-50 typography-caption-default"}
        >
          {valid ? <>&nbsp;</> : <>{helperText}</>}
        </Box>
      </Grid>
    </div>
  );
}

export { FileInput };
export type { FileInputProps };
