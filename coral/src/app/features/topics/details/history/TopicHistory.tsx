import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicOverview } from "src/domain/topic";

interface TopicHistoryRow {
  id: number;
  logs: string;
  team: string;
  requestedBy: string;
  requestedTime: string;
  approvedBy: string;
  approvedTime: string;
}

function TopicHistory() {
  const { topicOverview } = useTopicDetails();

  const columns: Array<DataTableColumn<TopicHistoryRow>> = [
    {
      type: "status",
      field: "logs",
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

  const historyList: TopicOverview["topicHistoryList"] =
    topicOverview.topicHistoryList;

  const rows: TopicHistoryRow[] =
    historyList?.map((historyEntry, index): TopicHistoryRow => {
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

  if (!rows.length) {
    return (
      <EmptyState title="No Topic history">
        This Topic contains no history.
      </EmptyState>
    );
  }

  return (
    <DataTable
      ariaLabel={"Topic history"}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
}

export { TopicHistory };
