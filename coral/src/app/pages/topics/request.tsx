import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import Layout from "src/app/layout/Layout";

const RequestTopic = () => {
  return (
    <Layout>
      <PreviewBanner linkTarget={"/requestTopics"} />
      <PageHeader title={"Request topic"} />
      <TopicRequest />
    </Layout>
  );
};

export default RequestTopic;
