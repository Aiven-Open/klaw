import { TopicRequests } from "src/app/features/requests/topics/TopicRequests";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";

const TopicRequestsPage = () => {
  return (
    <Layout>
      <PreviewBanner linkTarget={"/myTopicRequests"} />
      <TopicRequests />
    </Layout>
  );
};

export default TopicRequestsPage;
