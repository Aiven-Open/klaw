import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import { a11yLight } from "react-syntax-highlighter/dist/esm/styles/hljs";
import {
  Box,
  Button,
  Icon,
  SegmentedControl,
  SegmentedControlGroup,
} from "@aivenio/aquarium";
import classes from "src/app/components/documentation/documentation-editor.module.css";
import { useState } from "react";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";
import loading from "@aivenio/aquarium/icons/loading";
import { createStringifiedHtml } from "src/app/components/documentation/utils/topic-documentation-helper";

type DocumentationEditorProps = {
  documentation?: string;
  save: (documentation: string) => void;
  cancel: () => void;
  isSaving?: boolean;
};

type ViewMode = "edit" | "preview";
function DocumentationEditor({
  documentation,
  save,
  cancel,
  isSaving,
}: DocumentationEditorProps) {
  const [viewMode, setViewMode] = useState<ViewMode>("edit");
  const [text, setText] = useState(documentation || "");

  async function transformAndSave() {
    const stringifiedHtml = await createStringifiedHtml(text);
    save(stringifiedHtml);
  }

  return (
    <Box.Flex flexDirection={"column"} rowGap={"l1"}>
      <Box.Flex alignSelf={"end"}>
        <SegmentedControlGroup
          onChange={(value: ViewMode) => setViewMode(value)}
          value={viewMode}
        >
          <SegmentedControl value={"edit"}>Edit markdown</SegmentedControl>
          <SegmentedControl value={"preview"}>Preview</SegmentedControl>
        </SegmentedControlGroup>
      </Box.Flex>

      {viewMode === "edit" && (
        <div className={classes.markdownEditor}>
          <Box
            borderColor={"grey-20"}
            borderWidth={"1px"}
            borderRadius={"2px"}
            className={classes.markdownSyntaxHighlight}
          >
            <textarea
              value={text}
              onChange={(event) => setText(event?.target?.value)}
              className={classes.markdownTextarea}
            />
            <SyntaxHighlighter
              language={"markdown"}
              PreTag="div"
              style={a11yLight}
              wrapLongLines={true}
            >
              {text}
            </SyntaxHighlighter>
          </Box>
        </div>
      )}

      {viewMode === "preview" && <DocumentationView markdownString={text} />}

      <Box.Flex
        colGap={"l1"}
        paddingTop={"l2"}
        justifyContent={"end"}
        alignItems={"center"}
      >
        {isSaving && (
          <>
            <Icon icon={loading} fontSize={"30px"} />
            Saving documentation
          </>
        )}
        {!isSaving && (
          <>
            <Button.Secondary onClick={cancel}>Cancel</Button.Secondary>
            <Button.Primary onClick={transformAndSave}>
              Save documentation
            </Button.Primary>
          </>
        )}
      </Box.Flex>
    </Box.Flex>
  );
}

export { DocumentationEditor };
