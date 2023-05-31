import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";

function TopicOverview() {
  const { topicOverview } = useTopicDetails();

  return (
    <div>
      Placeholder for Overview panel, topic name:{" "}
      {topicOverview.topicInfoList[0].topicName}
    </div>
  );
}

export { TopicOverview };
