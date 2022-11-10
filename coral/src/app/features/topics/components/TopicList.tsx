import { Flexbox } from "@aivenio/design-system";
import { TopicCard } from "src/app/features/topics/components/TopicCard";
import { useGetTopics } from "src/app/features/topics/hooks/useGetTopics";

function TopicList() {
  const { data: topics, isLoading, isError } = useGetTopics();

  return (
    <>
      {isLoading && <div>Loading...</div>}
      {isError && <div>Something went wrong 😔</div>}

      {topics?.length === 0 && <div>No topics found</div>}
      {topics && (
        <Flexbox htmlTag={"ul"} colGap="l1" rowGap={"l1"} wrap={"wrap"}>
          {topics.map((topic) => {
            return (
              <TopicCard
                key={topic.topicid}
                description={topic.description}
                environmentsList={topic.environmentsList}
                teamname={topic.teamname}
                topicName={topic.topicName}
              />
            );
          })}
        </Flexbox>
      )}
    </>
  );
}
export { TopicList };
