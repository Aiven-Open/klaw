import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import { a11yLight } from "react-syntax-highlighter/dist/esm/styles/hljs";
import {
  Box,
  Button,
  SegmentedControl,
  SegmentedControlGroup,
  Typography,
} from "@aivenio/aquarium";
import classes from "src/app/components/documentation/documentation-editor.module.css";
import { useState } from "react";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";

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

  function saveDocumentation() {
    // trim to remove unnecessary whitespace at end/start
    const newDocumentation = text.trim();

    // existing `documentation` needs to be trimmed to compare, since
    // there we don't know if the value saved before migrating to React
    // contains spaces at beginning / end
    if (documentation && newDocumentation === documentation.trim()) return;
    save(newDocumentation);
  }

  return (
    <Box.Flex flexDirection={"column"} rowGap={"l1"}>
      <Box.Flex
        alignSelf={"end"}
        component={"section"}
        aria-label={"Switch between edit and preview mode"}
      >
        <SegmentedControlGroup
          onChange={(value: ViewMode) => setViewMode(value)}
          value={viewMode}
        >
          <SegmentedControl aria-pressed={viewMode === "edit"} value={"edit"}>
            Edit
          </SegmentedControl>
          <SegmentedControl
            aria-pressed={viewMode === "preview"}
            value={"preview"}
          >
            Preview
          </SegmentedControl>
        </SegmentedControlGroup>
      </Box.Flex>

      {viewMode === "edit" && (
        <div className={classes.markdownEditor}>
          <Box
            borderColor={"grey-20"}
            borderWidth={"1px"}
            borderRadius={"2px"}
            marginBottom={"l1"}
            className={classes.markdownSyntaxHighlight}
          >
            <label
              className={"visually-hidden"}
              htmlFor={"markdown-editor-textarea"}
            >
              Markdown editor
            </label>
            <textarea
              id={"markdown-editor-textarea"}
              aria-describedby={"editor-markdown-description"}
              value={text}
              onChange={(event) => setText(event?.target?.value)}
              className={classes.markdownTextarea}
            />
            <div aria-hidden={"true"}>
              <SyntaxHighlighter
                language={"markdown"}
                PreTag="div"
                style={a11yLight}
                wrapLongLines={true}
                customStyle={{ minHeight: "40vh" }}
              >
                {text}
              </SyntaxHighlighter>
            </div>
          </Box>
          <Typography.SmallTextBold id={"editor-markdown-description"}>
            We are supporting markdown following the{" "}
            <a href={"https://commonmark.org/help/"}>CommonMark</a> standard.
          </Typography.SmallTextBold>
        </div>
      )}

      {viewMode === "preview" && <DocumentationView markdownString={text} />}

      <Box.Flex
        colGap={"l1"}
        paddingTop={"l2"}
        justifyContent={"end"}
        alignItems={"center"}
      >
        <Button.Secondary onClick={cancel} loading={isSaving}>
          Cancel
        </Button.Secondary>
        <Button.Primary onClick={saveDocumentation} loading={isSaving}>
          Save documentation
        </Button.Primary>
      </Box.Flex>
    </Box.Flex>
  );
}

export { DocumentationEditor };
