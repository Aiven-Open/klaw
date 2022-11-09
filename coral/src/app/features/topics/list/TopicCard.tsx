import {
  BorderBox,
  Grid,
  GridItem,
  Typography,
  Chip,
  Flexbox,
  ExternalLinkButton,
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
    <li>
      <BorderBox
        style={{
          width: 400,
        }}
      >
        <Grid
          colGap={"l1"}
          rowGap={"l1"}
          style={{
            gridTemplateColumns: "1fr 1fr",
            gridTemplateRows: "auto 1fr 1fr",
            padding: 20,
          }}
        >
          <GridItem colSpan="span-2" className={classes.topicCardTitleDesc}>
            <Typography.Subheading htmlTag={"h2"}>
              {exampleCard.topicName}
            </Typography.Subheading>
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
          <GridItem colSpan="span-2" justifySelf={"start"}>
            <ExternalLinkButton href="/" kind="secondary">
              <span aria-hidden={"true"}>Topic overview</span>
              <span className={classes.visuallyHidden}>
                Overview for topic {exampleCard.topicName}
              </span>
            </ExternalLinkButton>
          </GridItem>
        </Grid>
      </BorderBox>
    </li>
  );
}

export { TopicCard };
