import { DataTable, DataTableColumn, Icon } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { Sources } from "KlawProvider";
import { SourcesContext } from "sourcesContext";
import loadingIcon from "@aivenio/aquarium/icons/loading";

interface TopicListProps<ParamsType extends Record<string, any>> {
  ariaLabel: string;
  params: ParamsType;
}

interface TopicsTableRow {
  id: number;
}

const TopicsTableBase = <ParamsType extends Record<string, any>>(
  props: TopicListProps<ParamsType> & { getTopics: Sources["getTopics"] }
) => {
  // Will not work in console (React v17)
  // const { getTopics } = useSourcesContext();
  const { ariaLabel, getTopics, params } = props;

  const { data } = useQuery({
    queryKey: ["getTopics", params],
    queryFn: () => getTopics(params),
    keepPreviousData: true,
  });

  if (data === undefined) {
    return <Icon icon={loadingIcon} />;
  }

  const columnNames = Object.keys((data?.topics || [])[0]);
  // @ts-ignore
  // Not sure why this error is popping up
  // Types of property 'field' are incompatible.
  // Type 'string' is not assignable to type '"id"'
  const columns: Array<DataTableColumn<TopicsTableRow>> = columnNames.map(
    (columnName) => {
      return {
        type: "text",
        headerName: columnName,
        field: columnName,
      };
    }
  );

  const rows: TopicsTableRow[] = (data?.topics || []).map((topic, index) => {
    const keys = Object.keys(topic);
    const values = keys.reduce((prev, curr) => {
      return { ...prev, [curr]: String(topic[curr]) };
    }, {});
    return {
      id: index,
      ...values,
    };
  });

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export const TopicsTable = <ParamsType extends Record<string, any>>(
  props: TopicListProps<ParamsType>
) => {
  return (
    <SourcesContext.Consumer>
      {({ getTopics }) => {
        return <TopicsTableBase {...props} getTopics={getTopics} />;
      }}
    </SourcesContext.Consumer>
  );
};
