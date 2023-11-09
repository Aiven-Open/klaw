import {
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
} from "@aivenio/aquarium";
import link from "@aivenio/aquarium/dist/src/icons/link";

interface Topic {
  topicName: string;
}

interface TopicListProps {
  topics: Topic[];
  ariaLabel: string;
}

interface TopicsTableRow {
  id: string;
  topicName: string;
}

export const TopicsTable = (props: TopicListProps) => {
  const { topics, ariaLabel } = props;

  const columns: Array<DataTableColumn<TopicsTableRow>> = [
    {
      type: "custom",
      headerName: "Topic",
      UNSAFE_render: ({ topicName }: TopicsTableRow) => {
        return (
          <>
            {topicName} <InlineIcon icon={link} />
          </>
        );
      },
    },
  ];

  const rows: TopicsTableRow[] = topics.map((topic: Topic, index) => {
    return {
      id: `${index}-${topic.topicName}`,
      topicName: topic.topicName,
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
};
