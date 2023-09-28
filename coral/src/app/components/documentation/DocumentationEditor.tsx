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
import { TopicDocumentationMarkdown } from "src/domain/topic";

type DocumentationEditorProps = {
  // using TopicDocumentation instead of string gives
  // a bit of type safety to make sure that up until this
  // place, we're only passing a markdown string that
  // we've created (and sanitized) ourselves

  documentation?: TopicDocumentationMarkdown;
  save: (documentation: TopicDocumentationMarkdown) => void;
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
  const [text, setText] = useState((documentation as string) || "");

  function saveDocumentation() {
    // trim to remove unnecessary whitespace at end/start
    const newDocumentation = text.trim();

    // existing `documentation` needs to be trimmed to compare, since
    // there we don't know if the value saved before migrating to React
    // contains spaces at beginning / end
    if (documentation && newDocumentation === documentation.trim()) return;
    save(newDocumentation as TopicDocumentationMarkdown);
  }

  return (
    <Box flexDirection={"column"} rowGap={"3"}>
      <Box
        alignSelf={"start"}
        component={"section"}
        aria-label={"Switch between edit and preview mode"}
      >
        <SegmentedControlGroup
          onChange={(value: ViewMode) => setViewMode(value)}
          value={viewMode}
        >
          <SegmentedControl<ViewMode>
            aria-pressed={viewMode === "edit"}
            value={"edit"}
          >
            Edit
          </SegmentedControl>
          <SegmentedControl<ViewMode>
            aria-pressed={viewMode === "preview"}
            value={"preview"}
          >
            Preview
          </SegmentedControl>
        </SegmentedControlGroup>
      </Box>

      {viewMode === "edit" && (
        <>
          <div className={classes.markdownEditor}>
            <Box
              borderColor={"grey-20"}
              borderWidth={"1px"}
              borderRadius={"2px"}
              marginBottom={"2"}
              className={classes.markdownSyntaxHighlight}
            >
              <label
                className={"visually-hidden"}
                htmlFor={"markdown-editor-textarea"}
              >
                Markdown editor
              </label>
              {/* The <textarea> is the element that takes and handles user */}
              {/* input and is accessible by assistive technology. It is presented */}
              {/* as not visible via the custom css styles (see css module) */}
              <textarea
                id={"markdown-editor-textarea"}
                aria-describedby={"editor-markdown-description"}
                value={text}
                onChange={(event) => setText(event?.target?.value)}
                className={classes.markdownTextarea}
                disabled={isSaving}
              />
              {/* The <SyntaxHighlighter> is showing the current value of the */}
              {/* <textarea> as an improved visual feedback for users. It is */}
              {/* hidden from assitive technology so this does not get the */}
              {/* same information twice */}
              <div aria-hidden={"true"}>
                <SyntaxHighlighter
                  language={"markdown"}
                  PreTag="div"
                  style={a11yLight}
                  wrapLongLines={true}
                  customStyle={{ minHeight: "35vh" }}
                >
                  {text}
                </SyntaxHighlighter>
              </div>
            </Box>
          </div>
          <Box justifyContent={"end"}>
            <Typography.SmallStrong id={"editor-markdown-description"}>
              We are supporting markdown following the{" "}
              <a href={"https://commonmark.org/help/"}>CommonMark</a> standard.
            </Typography.SmallStrong>
          </Box>
        </>
      )}

      {viewMode === "preview" && <DocumentationView markdownString={text} />}

      <Box colGap={"l1"} justifyContent={"start"} alignItems={"center"}>
        <Button.Secondary onClick={cancel} disabled={isSaving}>
          Cancel
        </Button.Secondary>
        <Button.Primary onClick={saveDocumentation} loading={isSaving}>
          {isSaving ? "Saving documentation" : "Save documentation"}
        </Button.Primary>
      </Box>
    </Box>
  );
}

export { DocumentationEditor };
