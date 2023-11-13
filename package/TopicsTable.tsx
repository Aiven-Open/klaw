import { DataTable, DataTableColumn, InlineIcon } from "@aivenio/aquarium";
import link from "@aivenio/aquarium/dist/src/icons/link";
import { useQuery } from "@tanstack/react-query";
import { Sources } from "KlawProvider";
import { SourcesContext } from "sourcesContext";

interface TopicListProps {
  ariaLabel: string;
  params: unknown;
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
  const { ariaLabel, getTopics, params } = props;

  const topics = useQuery({
    queryKey: ["getTopics", params],
    queryFn: () => getTopics(params),
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

  // Make a table that can take an arbitrary amount of data
  // take keys in response payload and render columns for them
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
