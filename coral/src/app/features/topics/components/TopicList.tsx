import { Flexbox } from "@aivenio/design-system";
import { TopicCard } from "src/app/features/topics/components/TopicCard";

const exampleCard = {
  topicid: 1010,
  sequence: "308",
  totalNoPages: "1",
  currentPage: "1",
  allPageNos: ["1"],
  topicName: "aivtopic1",
  noOfPartitions: 1,
  description: "Topic description",
  documentation: null,
  noOfReplcias: "2",
  teamname: "Ospo",
  cluster: "1",
  clusterId: null,
  environmentsList: ["DEV", "TST"],
  showEditTopic: false,
  showDeleteTopic: false,
  topicDeletable: false,
};

function TopicList() {
  return (
    <Flexbox htmlTag={"ul"} colGap="l2" rowGap={"l2"} wrap={"wrap"}>
      <TopicCard
        description={exampleCard.description}
        environmentsList={exampleCard.environmentsList}
        teamname={exampleCard.teamname}
        topicName={exampleCard.topicName}
      />
      <TopicCard
        description={exampleCard.description}
        environmentsList={exampleCard.environmentsList}
        teamname={exampleCard.teamname}
        topicName={exampleCard.topicName}
      />
      <TopicCard
        description={exampleCard.description}
        environmentsList={exampleCard.environmentsList}
        teamname={exampleCard.teamname}
        topicName={exampleCard.topicName}
      />
      <TopicCard
        description={exampleCard.description}
        environmentsList={exampleCard.environmentsList}
        teamname={exampleCard.teamname}
        topicName={exampleCard.topicName}
      />
    </Flexbox>
  );
}

export { TopicList };
