import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";
import { DocumentationEditView } from "src/app/features/topics/details/documentation/components/DocumentationEditView";
import { useEffect, useState } from "react";
import { createMarkdown } from "src/app/features/topics/details/documentation/utils/topic-documentation-helper";

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

  if (!topicDocumentation) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <NoDocumentationBanner />
      </>
    );
  }

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <DocumentationEditView
          documentation={topicOverview.topicDocumentation}
          cancelEdit={() => setEditMode(false)}
        />
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
