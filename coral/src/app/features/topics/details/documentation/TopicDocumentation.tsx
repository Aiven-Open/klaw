import { Alert, Box, PageHeader, useToast } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { useState } from "react";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";
import { useDocumentation } from "src/app/components/documentation/hooks/useDocumentation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateTopicDocumentation } from "src/domain/topic/topic-api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";

function TopicDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);

  const { topicOverview } = useTopicDetails();
  const { topicDocumentationMarkdownString } = useDocumentation(
    topicOverview.topicDocumentation
  );
  const toast = useToast();

  const { mutate, isError, isLoading, error } = useMutation(
    (stringifiedHtml: string) => {
      return updateTopicDocumentation({
        topicName: topicOverview.topicInfo.topicName,
        topicIdForDocumentation: topicOverview.topicIdForDocumentation,
        topicDocumentation: stringifiedHtml,
      });
    },
    {
      onSuccess: () => {
        setEditMode(false);
        queryClient.refetchQueries(["topic-overview"]).then(() => {
          toast({
            message: "Documentation successfully updated",
            position: "bottom-left",
            variant: "default",
          });
        });
      },
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
            documentation={topicDocumentationMarkdownString}
            save={(text) => mutate(text)}
            cancel={() => setEditMode(false)}
            isSaving={isLoading}
          />
        </>
      </>
    );
  }

  if (!topicDocumentationMarkdownString) {
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
      <DocumentationView markdownString={topicDocumentationMarkdownString} />
    </>
  );
}

export { TopicDocumentation };
