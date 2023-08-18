import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
  StatusChip,
} from "@aivenio/aquarium";
import link from "@aivenio/aquarium/dist/src/icons/link";
import { Link } from "react-router-dom";
import { Topic } from "src/domain/topic";
import { EnvironmentInfo } from "src/domain/environment/environment-types";

type TopicListProps = {
  topics: Topic[];
  ariaLabel: string;
};

interface TopicsTableRow {
  id: number;
  topicName: Topic["topicName"];
  environmentsList: EnvironmentInfo[];
  teamName: Topic["teamname"];
  envId: Topic["envId"];
}

function TopicTable(props: TopicListProps) {
  const { topics, ariaLabel } = props;

  const columns: Array<DataTableColumn<TopicsTableRow>> = [
    {
      type: "custom",
      headerName: "Topic",
      UNSAFE_render: ({ topicName, envId }: TopicsTableRow) => {
        return (
          <Link to={`/topic/${topicName}/overview`} state={envId}>
            {topicName} <InlineIcon icon={link} />
          </Link>
        );
      },
    },
    {
      type: "custom",
      headerName: "Environments",
      UNSAFE_render: ({ environmentsList }: TopicsTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            {environmentsList.map(({ name, id }) => (
              // Using a list conveys the information for
              // users with assistive technology better
              <li key={id}>
                <StatusChip dense status="neutral" text={name} />
              </li>
            ))}
          </Box.Flex>
        );
      },
    },
    {
      type: "text",
      field: "teamName",
      headerName: "Team",
    },
  ];

  const rows: TopicsTableRow[] = topics.map((topic: Topic) => {
    return {
      id: Number(topic.topicid),
      topicName: topic.topicName,
      teamName: topic.teamname,
      environmentsList: topic.environmentsList ?? [],
      envId: topic.envId,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Topics">
        No Topics matched your criteria.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}
export default TopicTable;
