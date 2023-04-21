import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Flexbox,
  StatusChip,
  InlineIcon,
} from "@aivenio/aquarium";
import { createTopicOverviewLink } from "src/app/features/topics/browse/utils/create-topic-overview-link";
import { Topic } from "src/domain/topic";
import link from "@aivenio/aquarium/dist/src/icons/link";

type TopicListProps = {
  topics: Topic[];
  ariaLabel: string;
};

interface TopicsTableRow {
  id: number;
  topicName: Topic["topicName"];
  environmentsList: string[];
  teamName: Topic["teamname"];
}

function TopicTable(props: TopicListProps) {
  const { topics, ariaLabel } = props;

  const columns: Array<DataTableColumn<TopicsTableRow>> = [
    {
      type: "custom",
      field: "topicName",
      headerName: "Topic",
      UNSAFE_render: ({ topicName }: TopicsTableRow) => (
        <a href={createTopicOverviewLink(topicName)}>
          {topicName} <InlineIcon icon={link} />
        </a>
      ),
    },
    {
      type: "custom",
      field: "environmentsList",
      headerName: "Environments",
      UNSAFE_render: ({ environmentsList }: TopicsTableRow) => {
        return (
          <Flexbox wrap={"wrap"} gap={"2"}>
            {environmentsList.map((env, index) => (
              <StatusChip
                dense
                status="neutral"
                key={`${env}-${index}`}
                // We need to add a space after text value
                // Otherwise a list of values would be rendered as value1value2value3 for screen readers
                // Instead of value1 value2 value3
                text={`${env} `}
              />
            ))}
          </Flexbox>
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
