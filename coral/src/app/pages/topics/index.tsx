import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { Flexbox, PageHeader } from "@aivenio/design-system";
import TopicCard from "src/app/features/topics/components/list/TopicCard";

const Topics = () => {
  return (
    <Flexbox direction={"column"} rowGap={"l2"}>
      <PageHeader title={"Browse all topics"} />
      <TopicCard
        topicName={"hello"}
        description={"hello"}
        teamname={"hello"}
        environmentsList={[]}
      />
      <BrowseTopics />
    </Flexbox>
  );
};

export default Topics;
