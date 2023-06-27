import { useState } from "react";
import {
  Box,
  Button,
  SegmentedControl,
  SegmentedControlGroup,
} from "@aivenio/aquarium";
import ReactMarkdown from "react-markdown";
import previewClasses from "src/app/features/topics/details/documentation/components/documentation-view.module.css";

type DocumentationEditorProps = {
  documentation?: string;
};

type ViewMode = "edit" | "preview";

function DocumentationEditor({ documentation }: DocumentationEditorProps) {
  const [text, setText] = useState("");
  const [viewMode, setViewMode] = useState<ViewMode>("edit");

  // only here to avoid linting error
  // the documentation coming from backend
  // needs to be transformed into markdown
  // in TopicDocumentation before we can pass
  // it here
  console.log(documentation);

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
        <textarea
          rows={5}
          style={{ border: "1px solid grey" }}
          defaultValue={"imagine the editor here, placeholder for now"}
          onChange={(event) => setText(event.target.value)}
        />
      )}

      {viewMode === "preview" && (
        <ReactMarkdown className={previewClasses.reactMarkdown}>
          {text}
        </ReactMarkdown>
      )}

      <Box.Flex colGap={"l1"} paddingTop={"l2"} justifyContent={"end"}>
        <Button.Secondary>Cancel edit</Button.Secondary>
        <Button.Primary>Save documentation</Button.Primary>
      </Box.Flex>
    </Box.Flex>
  );
}

export { DocumentationEditor };
