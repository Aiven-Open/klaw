import { useNavigate, useParams } from "react-router-dom";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";
import PreviewBanner from "src/app/components/PreviewBanner";

function TopicDetailsPage() {
  const { topicName } = useParams();
  const navigate = useNavigate();

  if (!topicName) {
    navigate("/topics");
    return <></>;
  }

  return (
    <>
      <PreviewBanner linkTarget={`/topicOverview?topicname=${topicName}`} />
      <TopicDetails topicName={topicName} />
    </>
  );
}

export { TopicDetailsPage };
