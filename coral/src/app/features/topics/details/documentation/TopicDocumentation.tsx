import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";

function TopicDocumentation() {
  // mocked data as topicOverview does not return doc right now
  const topicOverview = {
    topicDocumentation: "", //"<h1>hello</h1><div>This is doc</div>",
  };

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
