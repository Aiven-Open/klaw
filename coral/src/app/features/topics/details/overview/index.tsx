import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";

function TopicOverview() {
  const { topicName } = useTopicDetails();

  return <div>Placeholder for Overview panel, topic name: {topicName}</div>;
}

export { TopicOverview };
