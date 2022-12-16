import { Box, Chip, Table } from "@aivenio/aquarium";
import { Topic } from "src/domain/topic";
import classes from "src/app/features/browse-topics/components/topic-table/TopicTable.module.css";

type TopicListProps = {
  topics: Topic[];
  activePage: number;
  totalPages: number;
};

function TopicTable(props: TopicListProps) {
  const { topics, activePage, totalPages } = props;

  return (
    <Table ariaLabel={`Topics overview, page ${activePage} of ${totalPages}`}>
      <Table.Head>
        <Table.Cell scope={"col"} width={"60%"}>
          <span className={classes.topicTableColHead}>Topic</span>
        </Table.Cell>
        <Table.Cell scope={"col"}>
          {/*Workaround to get accessible color until DS ready */}
          <span className={classes.topicTableColHead}>Environments</span>
        </Table.Cell>
        <Table.Cell scope={"col"}>
          {/*Workaround to get accessible color until DS ready */}
          <span className={classes.topicTableColHead}>Team</span>
        </Table.Cell>
      </Table.Head>
      <Table.Body>
        {topics.map((topic) => (
          <Table.Row key={topic.topicid}>
            {/*Workaround to get right html element with scope*/}
            {/*until DS ready*/}
            <th
              scope="row"
              align={"left"}
              className={classes.topicTableRowHead}
            >
              <a href={`/topicOverview?topicname=${topic.topicName}`}>
                {topic.topicName}
              </a>
            </th>
            <Table.Cell>
              <Box
                component={"ul"}
                display={"flex"}
                alignItems={"center"}
                gap={"2"}
              >
                {topic.environmentsList.map((env, index) => {
                  return (
                    <li key={index + env}>
                      <Chip text={env} />
                    </li>
                  );
                })}
              </Box>
            </Table.Cell>
            <Table.Cell>{topic.teamname}</Table.Cell>
          </Table.Row>
        ))}
      </Table.Body>
    </Table>
  );
}
export default TopicTable;
