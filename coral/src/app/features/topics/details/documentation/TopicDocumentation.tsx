import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";

function TopicDocumentation() {
  const { topicOverview } = useTopicDetails();

  return (
    <>
      <PageHeader
        title={"Documentation"}
        primaryAction={{
          text: "Edit documentation",
          onClick: () => console.log("update"),
        }}
      />
      {topicOverview.topicDocumentation === undefined ||
      topicOverview.topicDocumentation.length === 0 ? (
        <NoDocumentationBanner />
      ) : (
        <DocumentationView stringifiedHtml={topicOverview.topicDocumentation} />
      )}
    </>
  );
}

export { TopicDocumentation };
