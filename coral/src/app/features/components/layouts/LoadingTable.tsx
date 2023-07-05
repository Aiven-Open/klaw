import {
  DataTable,
  DataTableColumn,
  EmptyState,
  Skeleton,
} from "@aivenio/aquarium";
import { DataTableRow } from "@aivenio/aquarium/dist/src/utils/table/types";

type LoadingTableColumn = {
  headerName: string;
  width?: number | `${number}%`;
  headerInvisible?: boolean;
};
type LoadingTableProps = {
  rowLength: number;
  columns: LoadingTableColumn[];
};

//eslint-disable-next-line @typescript-eslint/no-explicit-any

function LoadingTable({ rowLength, columns }: LoadingTableProps) {
  if (rowLength === 0) {
    return (
      <EmptyState title="No subscriptions">
        No subscription matched your criteria.
      </EmptyState>
    );
  }
  const columnsEmptyTable = columns.map((column, index) => {
    return {
      id: index,
      type: "custom" as DataTableRow["type"],
      headerName: column.headerName,
      width: column.width,
      headerInvisible: column.headerInvisible,
      UNSAFE_render: () => {
        return <Skeleton key={index} />;
      },
    };
  });

  const rows = Array(rowLength)
    .fill("loading")
    .map((header, index) => {
      return { [header]: index, id: index };
    });

  return (
    <DataTable columns={columnsEmptyTable} rows={rows} ariaLabel={"Loading"} />
  );
}

export { LoadingTable };
