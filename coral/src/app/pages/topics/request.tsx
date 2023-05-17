import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicRequest from "src/app/features/topics/request/TopicRequest";

const RequestTopic = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/requestTopics"} />
      <PageHeader title={"Request topic"} />
      <TopicRequest />
    </>
  );
};

export default RequestTopic;
