import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { DocumentationViewOnly } from "src/app/features/topics/details/documentation/components/DocumentationViewOnly";
import { DocumentationEditor } from "src/app/features/topics/details/documentation/components/DocumentationEditor";
import { useState } from "react";

function TopicDocumentation() {
  const { topicOverview } = useTopicDetails();
  const [editMode, setEditMode] = useState(false);

  if (
    topicOverview.topicDocumentation === undefined ||
    topicOverview.topicDocumentation.length === 0
  ) {
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
        <DocumentationEditor documentation={topicOverview.topicDocumentation} />
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
      <DocumentationViewOnly
        stringifiedHtml={topicOverview.topicDocumentation}
      />
    </>
  );
}

export { TopicDocumentation };
