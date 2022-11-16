import { Flexbox } from "@aivenio/design-system";
import TopicCard from "src/app/features/topics/list/components/TopicCard";
import { Topic } from "src/domain/topics";

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
