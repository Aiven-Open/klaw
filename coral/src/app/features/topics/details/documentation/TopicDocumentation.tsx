import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { DocumentationEditView } from "src/app/features/topics/details/documentation/components/DocumentationEditView";
import { useEffect, useState } from "react";
import { createMarkdown } from "src/app/components/documentation/utils/topic-documentation-helper";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";

function TopicDocumentation() {
  const { topicOverview } = useTopicDetails();
  const [topicDocumentation, setTopicDocumentation] = useState<
    string | undefined
  >();
  const [editMode, setEditMode] = useState(false);

  useEffect(() => {
    // It would be more clear and responsibilities better split
    // to do that on API level, so outside from 'domain' we don't
    // even know that we handle stringified html, but that would
    // require to do that on all topicOverview entries and is
    // unnecessary load for user
    if (topicOverview.topicDocumentation !== undefined) {
      const docToTransform = topicOverview.topicDocumentation;
      const transformDocumentationString = async () => {
        const documentation = await createMarkdown(docToTransform);
        setTopicDocumentation(documentation);
      };
      transformDocumentationString();
    }
  }, [topicOverview.topicDocumentation]);

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <DocumentationEditView
          topicName={topicOverview.topicInfo.topicName}
          topicIdForDocumentation={topicOverview.topicIdForDocumentation}
          documentation={topicDocumentation}
          closeEditView={() => setEditMode(false)}
        />
      </>
    );
  }

  if (!topicDocumentation) {
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
      <DocumentationView markdownString={topicDocumentation} />
    </>
  );
}

export { TopicDocumentation };
