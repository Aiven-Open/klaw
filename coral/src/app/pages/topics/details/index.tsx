import { Navigate, useParams } from "react-router-dom";
import { TopicDetails } from "src/app/features/topics/details/TopicDetails";

function TopicDetailsPage() {
  const { topicName } = useParams();

  if (!topicName) {
    return <Navigate to="/topics" replace={true} />;
  }

  return <TopicDetails topicName={topicName} />;
}

export { TopicDetailsPage };
