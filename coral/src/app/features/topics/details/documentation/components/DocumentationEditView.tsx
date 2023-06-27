import { useState } from "react";
import {
  Box,
  Button,
  SegmentedControl,
  SegmentedControlGroup,
} from "@aivenio/aquarium";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";
import { DocumentationEditor } from "src/app/features/topics/details/documentation/components/DocumentationEditor";

type DocumentationEditViewProps = {
  documentation?: string;
  cancelEdit: () => void;
};

type ViewMode = "edit" | "preview";

function saveDocumentation(doc: string) {
  console.log("save documentation mock:");
  console.log(doc);
}
function DocumentationEditView({
  documentation,
  cancelEdit,
}: DocumentationEditViewProps) {
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

      {viewMode === "edit" && (
        <DocumentationEditor
          documentation={text}
          onChangeDocumentation={setText}
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
