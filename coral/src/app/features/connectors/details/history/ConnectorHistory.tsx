import {
  DataTable,
  DataTableColumn,
  EmptyState,
  PageHeader,
} from "@aivenio/aquarium";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorOverview } from "src/domain/connector";
import { LoadingTable } from "src/app/features/components/layouts/LoadingTable";

interface ConnectorHistoryRow {
  id: number;
  logs: string;
  team: string;
  requestedBy: string;
  requestedTime: string;
  approvedBy: string;
  approvedTime: string;
}

function ConnectorHistory() {
  const { connectorOverview, connectorIsRefetching } = useConnectorDetails();

  const columns: Array<DataTableColumn<ConnectorHistoryRow>> = [
    {
      type: "status",
      headerName: "Logs",
      status: ({ logs }) => ({
        status: "neutral",
        text: logs,
      }),
    },
    { type: "text", field: "team", headerName: "Team" },
    { type: "text", field: "requestedBy", headerName: "Requested by" },
    {
      type: "text",
      field: "requestedTime",
      headerName: "Requested on",
      formatter: (value) => {
        return `${value}${"\u00A0"}UTC`;
      },
    },
    { type: "text", field: "approvedBy", headerName: "Approved by" },
    {
      type: "text",
      field: "approvedTime",
      headerName: "Approved on",
      formatter: (value) => {
        return `${value}${"\u00A0"}UTC`;
      },
    },
  ];

  const loadingColumns = columns.map((col) => {
    return {
      headerName: col.headerName,
      width: col.width,
      headerVisible: col.headerInvisible,
    };
  });

  const historyList: ConnectorOverview["connectorHistoryList"] =
    connectorOverview.connectorHistoryList;

  const rows: ConnectorHistoryRow[] =
    historyList?.map((historyEntry, index): ConnectorHistoryRow => {
      return {
        id: index,
        logs: historyEntry.remarks,
        team: historyEntry.teamName,
        requestedBy: historyEntry.requestedBy,
        requestedTime: historyEntry.requestedTime,
        approvedBy: historyEntry.approvedBy,
        approvedTime: historyEntry.approvedTime,
      };
    }) || [];

  const loadingRowLength = rows.length === 0 ? 1 : rows.length;
  return (
    <>
      <PageHeader title={"History"} />
      {connectorIsRefetching ? (
        <LoadingTable rowLength={loadingRowLength} columns={loadingColumns} />
      ) : (
        <>
          {rows.length === 0 && (
            <EmptyState title="No Connector history">
              This connector contains no history.
            </EmptyState>
          )}
          {rows.length > 0 && (
            <DataTable
              ariaLabel={"Connector history"}
              columns={columns}
              rows={rows}
              noWrap={false}
            />
          )}
        </>
      )}
    </>
  );
}

export { ConnectorHistory };
