import { Alert, Box, PageHeader, Skeleton, useToast } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  TopicDocumentationMarkdown,
  updateTopicDocumentation,
} from "src/domain/topic";
import { parseErrorMsg } from "src/services/mutation-utils";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";
import { isDocumentationTransformationError } from "src/domain/helper/documentation-helper";

function TopicDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const { topicOverview, topicOverviewIsRefetching } = useTopicDetails();

  const toast = useToast();

  const { mutate, isError, error } = useMutation(
    (markdownString: TopicDocumentationMarkdown) => {
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
      onError: () => {
        setSaving(false);
      },
    }
  );

  if (topicOverviewIsRefetching) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <Box paddingTop={"l2"}>
          <div className={"visually-hidden"}>Loading documentation</div>
          <Skeleton />
        </Box>
      </>
    );
  }

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <>
          {isError && (
            <Box marginBottom={"l1"}>
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

  if (isDocumentationTransformationError(topicOverview.topicDocumentation)) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <Alert type="error">
          Something went wrong while trying to transform the documentation into
          the right format.
        </Alert>
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
      <Box paddingTop={"l2"}>
        <DocumentationView markdownString={topicOverview.topicDocumentation} />
      </Box>
    </>
  );
}

export { TopicDocumentation };
