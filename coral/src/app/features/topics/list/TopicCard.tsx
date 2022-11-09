import { BorderBox, Grid, GridItem } from "@aivenio/design-system";

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

function TopicCard() {
  return (
    <BorderBox
      style={{
        padding: 10,
        width: 400,
      }}
    >
      <Grid
        colGap={"l1"}
        style={{
          gridTemplateColumns: "auto",
          gridTemplateRows: "1fr 1fr",
        }}
      >
        <GridItem colSpan="span-2">
          <div>{exampleCard.topicName}</div>
          <div>{exampleCard.description}</div>
        </GridItem>
        <GridItem>
          <div>Owner</div>
          <div>{exampleCard.teamname}</div>
        </GridItem>
        <GridItem>
          <div>Environments:</div>
          {exampleCard.environmentsList.map((env, index) => {
            return <div key={env + index}>{env}</div>;
          })}
        </GridItem>
      </Grid>
    </BorderBox>
  );
}

export { TopicCard };
