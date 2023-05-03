import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicRequest from "src/app/features/topics/request/TopicRequest";

const RequestTopic = () => {
  return (
    <AuthenticationRequiredBoundary>
      <>
        <PreviewBanner linkTarget={"/requestTopics"} />
        <PageHeader title={"Request topic"} />
        <TopicRequest />
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default RequestTopic;
