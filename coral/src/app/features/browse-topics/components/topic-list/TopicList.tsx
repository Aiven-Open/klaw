import { Flexbox } from "@aivenio/design-system";
import { Topic } from "src/domain/topic";
import TopicCard from "src/app/features/browse-topics/components/topic-list/TopicCard";

type TopicListProps = {
  topics: Topic[];
};

function TopicList(props: TopicListProps) {
  const { topics } = props;
  return (
    <ul aria-label={"Topics"}>
      <Flexbox colGap="l1" rowGap={"l1"} wrap={"wrap"}>
        {topics.map((topic) => {
          return (
            <li key={topic.topicid}>
              <TopicCard
                description={topic.description}
                environmentsList={topic.environmentsList}
                teamname={topic.teamname}
                topicName={topic.topicName}
              />
            </li>
          );
        })}
      </Flexbox>
    </ul>
  );
}
export default TopicList;
