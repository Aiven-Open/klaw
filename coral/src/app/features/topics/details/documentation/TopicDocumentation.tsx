import { Alert, Box, PageHeader, useToast } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateTopicDocumentation } from "src/domain/topic";
import { parseErrorMsg } from "src/services/mutation-utils";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";

function TopicDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const { topicOverview } = useTopicDetails();

  const toast = useToast();

  const { mutate, isError, error } = useMutation(
    (markdownString: string) => {
      setSaving(true);
      return updateTopicDocumentation({
        topicName: topicOverview.topicInfo.topicName,
        topicIdForDocumentation: topicOverview.topicIdForDocumentation,
        topicDocumentation: markdownString,
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
          setSaving(false);
          setEditMode(false);
        });
      },
      onError: () => setEditMode(false),
    }
  );

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <>
          {isError && (
            <Box marginBottom={"l1"} role="alert">
              <Alert type="error">
                The documentation could not be saved, there was an error: <br />
                {parseErrorMsg(error)}
              </Alert>
            </Box>
          )}
          <DocumentationEditor
            documentation={topicOverview.topicDocumentation}
            save={(text) => mutate(text)}
            cancel={() => setEditMode(false)}
            isSaving={saving}
          />
        </>
      </>
    );
  }

  if (
    !topicOverview.topicDocumentation ||
    topicOverview.topicDocumentation.length === 0
  ) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <NoDocumentationBanner addDocumentation={() => setEditMode(true)} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={"Documentation"}
        primaryAction={{
          text: "Edit documentation",
          onClick: () => setEditMode(true),
        }}
      />
      <DocumentationView markdownString={topicOverview.topicDocumentation} />
    </>
  );
}

export { TopicDocumentation };
