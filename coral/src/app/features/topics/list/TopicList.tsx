import { Flexbox } from "@aivenio/design-system";
import { TopicCard } from "src/app/features/topics/list/TopicCard";

function TopicList() {
  return (
    <Flexbox htmlTag={"ul"} colGap="l2" rowGap={"l2"} wrap={"wrap"}>
      <TopicCard />
      <TopicCard />
      <TopicCard />
      <TopicCard />
    </Flexbox>
  );
}

export { TopicList };
