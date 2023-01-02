import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import Layout from "src/app/layout/Layout";

const RequestTopic = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PreviewBanner linkTarget={"/requestTopics"} />
        <PageHeader title={"Request topic"} />
        <TopicRequest />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default RequestTopic;
