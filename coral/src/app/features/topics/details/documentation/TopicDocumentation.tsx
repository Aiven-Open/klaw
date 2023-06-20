import { PageHeader } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";

function TopicDocumentation() {
  // mocked data as topicOverview does not return doc right now
  const topicOverview = {
    topicDocumentation: "", //"<h1>hello</h1><div>This is doc</div>",
  };

  const noDocumentation =
    !topicOverview?.topicDocumentation ||
    topicOverview.topicDocumentation.length === 0;

  return (
    <>
      <PageHeader title={"Documentation"} />
      {noDocumentation && <NoDocumentationBanner />}
      <div>{topicOverview.topicDocumentation}</div>
    </>
  );
}

export { TopicDocumentation };
