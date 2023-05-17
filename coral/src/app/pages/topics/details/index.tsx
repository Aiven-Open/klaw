import { useNavigate, useParams } from "react-router-dom";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";

function TopicDetailsPage() {
  const { topicName } = useParams();
  const navigate = useNavigate();

  if (!topicName) {
    navigate("/topics");
    return <></>;
  }

  return <TopicDetails topicName={topicName} />;
}

export { TopicDetailsPage };
