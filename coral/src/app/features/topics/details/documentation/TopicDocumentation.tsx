import {
  Alert,
  Box,
  PageHeader,
  Skeleton,
  Typography,
  useToast,
} from "@aivenio/aquarium";
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
import { NoDocumentationBanner } from "src/app/features/components/documentation/components/NoDocumentationBanner";

const readmeDescription = (topicOwner: boolean) => {
  const fixedText = `Readme provides essential information, guidelines, and explanations about the topic, helping team members understand its purpose and usage.`;
  const additionalTextTopicOwner = `Edit the readme to update the information as the topic evolves.`;
  return (
    <Box component={Typography.SmallText} marginBottom={"l2"}>
      {fixedText} {topicOwner ? additionalTextTopicOwner : ""}
    </Box>
  );
};
function TopicDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const { topicOverview, topicOverviewIsRefetching } = useTopicDetails();

  const isUserTopicOwner = Boolean(topicOverview.topicInfo.topicOwner);

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
            message: "Readme successfully updated",
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

  if (isUserTopicOwner && topicOverviewIsRefetching) {
    return (
      <>
        <PageHeader title={"Readme"} />
        <Box paddingTop={"l2"}>
          <div className={"visually-hidden"}>Loading readme</div>
          <Skeleton />
        </Box>
      </>
    );
  }

  if (isUserTopicOwner && editMode) {
    return (
      <>
        <PageHeader title={"Edit readme"} />
        {readmeDescription(isUserTopicOwner)}

        {isError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">
              The readme could not be saved, there was an error: <br />
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
    );
  }

  if (
    !topicOverview.topicDocumentation ||
    topicOverview.topicDocumentation.length === 0
  ) {
    return (
      <>
        <PageHeader title={"Readme"} />
        <NoDocumentationBanner
          addDocumentation={() => setEditMode(true)}
          isUserOwner={isUserTopicOwner}
          entity={"topic"}
        />
      </>
    );
  }

  if (
    isUserTopicOwner &&
    isDocumentationTransformationError(topicOverview.topicDocumentation)
  ) {
    return (
      <>
        <PageHeader title={"Readme"} />
        <Alert type="error">
          Something went wrong while trying to transform the readme into the
          right format.
        </Alert>
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={"Readme"}
        primaryAction={
          isUserTopicOwner
            ? {
                text: "Edit readme",
                onClick: () => setEditMode(true),
              }
            : undefined
        }
      />
      {readmeDescription(isUserTopicOwner)}

      <Box paddingTop={"l2"}>
        <DocumentationView markdownString={topicOverview.topicDocumentation} />
      </Box>
    </>
  );
}

export { TopicDocumentation };
