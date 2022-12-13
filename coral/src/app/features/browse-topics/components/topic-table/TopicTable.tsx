import { Chip, Flexbox, Table } from "@aivenio/design-system";
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
        {topics.map((dot) => (
          <Table.Row key={dot.topicid}>
            {/*Workaround to get right html element with scope*/}
            {/*until DS ready*/}
            <th
              scope="row"
              align={"left"}
              className={classes.topicTableRowHead}
            >
              <a href={`/topicOverview?topicname=${dot.topicName}`}>
                {dot.topicName}
              </a>
            </th>
            <Table.Cell>
              <Flexbox htmlTag={"ul"} alignItems={"center"} gap={"2"}>
                {dot.environmentsList.map((env, index) => {
                  return (
                    <li key={index + env}>
                      <Chip text={env} />
                    </li>
                  );
                })}
              </Flexbox>
            </Table.Cell>
            <Table.Cell>{dot.teamname}</Table.Cell>
          </Table.Row>
        ))}
      </Table.Body>
    </Table>
  );
}
export default TopicTable;
