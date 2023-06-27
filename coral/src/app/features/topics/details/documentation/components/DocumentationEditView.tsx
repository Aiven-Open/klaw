import { useState } from "react";
import {
  Box,
  Button,
  SegmentedControl,
  SegmentedControlGroup,
} from "@aivenio/aquarium";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";

type DocumentationEditorProps = {
  documentation?: string;
  cancelEdit: () => void;
};

type ViewMode = "edit" | "preview";

function saveDocumentation(doc: string) {
  console.log("save documentation mock");
  console.log(doc);
}
function DocumentationEditView({
  documentation,
  cancelEdit,
}: DocumentationEditorProps) {
  const [text, setText] = useState(documentation || "");
  const [viewMode, setViewMode] = useState<ViewMode>("edit");

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

      {/*PLACEHOLDER*/}
      {viewMode === "edit" && (
        <textarea
          value={text}
          rows={5}
          style={{ border: "1px solid grey" }}
          onChange={(event) => setText(event.target.value)}
        />
      )}

      {viewMode === "preview" && <DocumentationView markdownString={text} />}

      <Box.Flex colGap={"l1"} paddingTop={"l2"} justifyContent={"end"}>
        <Button.Secondary onClick={cancelEdit}>Cancel edit</Button.Secondary>
        <Button.Primary onClick={() => saveDocumentation(text)}>
          Save documentation
        </Button.Primary>
      </Box.Flex>
    </Box.Flex>
  );
}

export { DocumentationEditView };
