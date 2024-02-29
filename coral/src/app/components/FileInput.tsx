import { Box, Grid, Icon, Typography } from "@aivenio/aquarium";
import cloudUpload from "@aivenio/aquarium/dist/src/icons/cloudUpload";
import omit from "lodash/omit";
import uniqueId from "lodash/uniqueId";
import { InputHTMLAttributes, useRef } from "react";
import classes from "src/app/components/FileInput.module.css";
import { ResolveIntersectionTypes } from "types/utils";

type FileInputProps = ResolveIntersectionTypes<
  InputHTMLAttributes<HTMLInputElement> & {
    valid: boolean;
    /** labelText is needed to transport important
     * information to visual and AT users */
    labelText: string;
    /** buttonText is not conveyed to e.g. screen reader
     * users, treat is as more decorative text */
    buttonText: string;
    helperText: string;
    fileName?: string;
  }
>;

const EMPTY_FILE_NAME = "No file chosen";

function FileInput(props: FileInputProps) {
  const {
    valid,
    labelText,
    buttonText,
    helperText,
    fileName = EMPTY_FILE_NAME,
  } = props;

  const inputRef = useRef<null | HTMLInputElement>(null);
  const inputId = uniqueId("file_upload_");
  const errorMessageId = uniqueId("file_upload_error_message");

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
        <Typography.Caption className={classes.fakeLabel}>
          <span className={!valid ? "text-error-50" : ""}>{labelText}</span>
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
        <Grid.Item colStart={"1"} colEnd={"1"} rowStart={"1"} rowEnd={"1"}>
          <Box
            aria-hidden={true}
            display={"flex"}
            alignItems={"center"}
            backgroundColor={"white"}
            className={classes.fakeButton}
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
        </Grid.Item>

        <Grid.Item colStart={"2"} colEnd={"2"} rowStart={"1"} rowEnd={"1"}>
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
            {fileName}
          </Box>
        </Grid.Item>

        <Grid.Item
          className={classes.fileInputWrapper}
          colStart={"1"}
          colEnd={"1"}
          rowStart={"1"}
          rowEnd={"1"}
        >
          <label htmlFor={inputId}>
            <span className={"visually-hidden"}>
              {fileName !== EMPTY_FILE_NAME
                ? `Uploaded file, name: ${fileName}. Click to upload new file.`
                : labelText}
            </span>
            <input
              {...inputAttributes}
              id={inputId}
              type={"file"}
              ref={inputRef}
              aria-required={props.required}
              aria-invalid={!valid}
              className={classes.fileInput}
              {...(!valid && { "aria-describedby": errorMessageId })}
            />
          </label>
        </Grid.Item>
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
