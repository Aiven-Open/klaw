import {
  BorderBox,
  Grid,
  GridItem,
  Typography,
  Chip,
  Flexbox,
} from "@aivenio/design-system";
import classes from "src/app/features/topics/list/TopicCard.module.css";

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
        width: 400,
      }}
    >
      <Grid
        colGap={"l1"}
        style={{
          gridTemplateColumns: "1fr 1fr",
          gridTemplateRows: "auto 1fr",
          padding: 20,
        }}
      >
        <GridItem colSpan="span-2" className={classes.topicCardTitleDesc}>
          <Typography.Heading htmlTag={"h2"}>
            {exampleCard.topicName}
          </Typography.Heading>
          <div>{exampleCard.description}</div>
        </GridItem>
        <GridItem>
          <strong>Owner</strong>
          <div>{exampleCard.teamname}</div>
        </GridItem>
        <GridItem>
          <strong>Environments:</strong>
          <Flexbox colGap="l1" rowGap={"l1"} wrap={"wrap"}>
            {exampleCard.environmentsList.map((env, index) => {
              return <Chip key={env + index} text={env} />;
            })}
          </Flexbox>
        </GridItem>
      </Grid>
    </BorderBox>
  );
}

export { TopicCard };
