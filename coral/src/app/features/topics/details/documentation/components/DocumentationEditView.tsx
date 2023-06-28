import { useState } from "react";
import {
  Alert,
  Box,
  Button,
  Icon,
  SegmentedControl,
  SegmentedControlGroup,
  useToast,
} from "@aivenio/aquarium";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { parseErrorMsg } from "src/services/mutation-utils";
import { updateTopicDocumentation } from "src/domain/topic/topic-api";
import loading from "@aivenio/aquarium/icons/loading";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";

type DocumentationEditViewProps = {
  documentation?: string;
  closeEditView: () => void;
  topicName: string;
  topicIdForDocumentation: number;
};

type ViewMode = "edit" | "preview";

function DocumentationEditView({
  documentation,
  closeEditView,
  topicName,
  topicIdForDocumentation,
}: DocumentationEditViewProps) {
  const queryClient = useQueryClient();
  const [text, setText] = useState(documentation || "");
  const [viewMode, setViewMode] = useState<ViewMode>("edit");

  // isloading from mutate is false again as soon as mutation was successful,
  // but we want to make sure we only remove the loading animation when data is refetched
  // in the background
  const [saving, setSaving] = useState(false);

  const toast = useToast();

  const { mutate, isError, error } = useMutation(
    (markdownString: string) => {
      setSaving(true);
      return updateTopicDocumentation({
        topicName,
        topicIdForDocumentation,
        markdownString,
      });
    },
    {
      onSuccess: () => {
        queryClient.refetchQueries(["topic-overview"]).then(() => {
          toast({
            message: "Documentation successfully updated",
            position: "bottom-left",
            variant: "default",
          });
          closeEditView();
        });
      },
      onError: () => setSaving(false),
    }
  );

  return (
    <>
      {isError && (
        <Box marginBottom={"l1"} role="alert">
          <Alert type="error">
            The documentation could not be saved, there was an error: <br />
            {parseErrorMsg(error)}
          </Alert>
        </Box>
      )}
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

        <Box.Flex
          colGap={"l1"}
          paddingTop={"l2"}
          justifyContent={"end"}
          alignItems={"center"}
        >
          {saving && (
            <>
              <Icon icon={loading} fontSize={"30px"} />
              Saving documentation
            </>
          )}
          {!saving && (
            <>
              <Button.Secondary onClick={closeEditView}>
                Cancel edit
              </Button.Secondary>
              <Button.Primary onClick={() => mutate(text)}>
                Save documentation
              </Button.Primary>
            </>
          )}
        </Box.Flex>
      </Box.Flex>
    </>
  );
}

export { DocumentationEditView };
