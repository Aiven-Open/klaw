import TopicRequests from "src/app/features/requests/topics/TopicRequests";
import PreviewBanner from "src/app/components/PreviewBanner";

const TopicRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/myTopicRequests"} />
      <TopicRequests />
    </>
  );
};

export default TopicRequestsPage;
