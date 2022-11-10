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
import { Topic } from "src/domain/topics";

type TopicCardPros = Pick<
  Topic,
  "topicName" | "description" | "teamname" | "environmentsList"
>;

function TopicCard(props: TopicCardPros) {
  const { topicName, description, teamname, environmentsList } = props;

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
              {topicName}
            </Typography.Subheading>
            <div>{description}</div>
          </GridItem>
          <GridItem>
            <strong>Owner</strong>
            <div>{teamname}</div>
          </GridItem>
          <GridItem>
            <strong>Environments</strong>
            <Flexbox colGap="l1" rowGap={"l1"} wrap={"wrap"}>
              {environmentsList.map((env, index) => {
                return <Chip key={env + index} text={env} />;
              })}
            </Flexbox>
          </GridItem>
          <GridItem colSpan="span-2" justifySelf={"start"}>
            <ExternalLinkButton href="/" kind="secondary">
              <span aria-hidden={"true"}>Topic overview</span>
              <span className={classes.visuallyHidden}>
                Overview for topic {topicName}
              </span>
            </ExternalLinkButton>
          </GridItem>
        </Grid>
      </BorderBox>
    </li>
  );
}

export { TopicCard };
