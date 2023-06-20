import { PageHeader } from "@aivenio/aquarium";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";

function TopicDocumentation() {
  const { topicOverview } = useTopicDetails();
  return (
    <>
      <PageHeader title={"Documentation"} />
      <div>{topicOverview.topicDocumentation}</div>
    </>
  );
}

export { TopicDocumentation };
