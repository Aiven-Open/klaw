import {
  DataTable,
  DataTableColumn,
  EmptyState,
  InlineIcon,
} from "@aivenio/aquarium";
import data from "@aivenio/aquarium/dist/src/icons/link";
import link from "@aivenio/aquarium/dist/src/icons/link";
import { useQuery } from "@tanstack/react-query";
import { Sources } from "KlawProvider";
import { SourcesContext } from "sourcesContext";

interface TopicListProps {
  ariaLabel: string;
}

interface TopicsTableRow {
  id: string;
  topicName: string;
}

const TopicsTableBase = (
  props: TopicListProps & { getTopics: Sources["getTopics"] }
) => {
  // Will not work in console (React v17)
  // const { getTopics } = useSourcesContext();
  const { ariaLabel, getTopics } = props;

  const topics = useQuery({
    queryKey: ["getTopics"],
    queryFn: () => getTopics,
    keepPreviousData: true,
  });

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

  const rows: TopicsTableRow[] = (topics?.data?.topics || []).map(
    ({ topicName }, index) => {
      return {
        id: `${index}-${topicName}`,
        topicName: topicName,
      };
    }
  );

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export const TopicsTable = (props: TopicListProps) => {
  return (
    <SourcesContext.Consumer>
      {({ getTopics }) => {
        return <TopicsTableBase {...props} getTopics={getTopics} />;
      }}
    </SourcesContext.Consumer>
  );
};
