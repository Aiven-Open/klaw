import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Flexbox,
  InlineIcon,
  StatusChip,
} from "@aivenio/aquarium";
import link from "@aivenio/aquarium/dist/src/icons/link";
import { Link } from "react-router-dom";
import { createTopicOverviewLink } from "src/app/features/topics/browse/utils/create-topic-overview-link";
import { Topic } from "src/domain/topic";
import useFeatureFlag from "src/services/feature-flags/hook/useFeatureFlag";
import { FeatureFlag } from "src/services/feature-flags/types";

type TopicListProps = {
  topics: Topic[];
  ariaLabel: string;
};

interface TopicsTableRow {
  id: number;
  topicName: Topic["topicName"];
  environmentsList: string[];
  teamName: Topic["teamname"];
  envId: Topic["envId"];
}

function TopicTable(props: TopicListProps) {
  const { topics, ariaLabel } = props;
  const topicDetailsEnabled = useFeatureFlag(
    FeatureFlag.FEATURE_FLAG_TOPIC_OVERVIEW
  );

  const columns: Array<DataTableColumn<TopicsTableRow>> = [
    {
      type: "custom",
      headerName: "Topic",
      UNSAFE_render: ({ topicName, envId }: TopicsTableRow) => {
        if (!topicDetailsEnabled) {
          return (
            <a href={createTopicOverviewLink(topicName)}>
              {topicName} <InlineIcon icon={link} />
            </a>
          );
        }
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
