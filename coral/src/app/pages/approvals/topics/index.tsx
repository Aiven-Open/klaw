import PreviewBanner from "src/app/components/PreviewBanner";
import TopicApprovals from "src/app/features/approvals/topics/TopicApprovals";

const TopicApprovalsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/execTopics"} />
      <TopicApprovals />
    </>
  );
};

export default TopicApprovalsPage;
