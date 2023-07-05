import {
  DataTable,
  DataTableColumn,
  EmptyState,
  PageHeader,
  Skeleton,
} from "@aivenio/aquarium";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicOverview } from "src/domain/topic";
import { LoadingTable } from "src/app/features/components/layouts/LoadingTable";

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
  const { topicOverview, topicOverviewIsRefetching } = useTopicDetails();

  const columns: Array<DataTableColumn<TopicHistoryRow>> = [
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

  return (
    <>
      <PageHeader title={"History"} />
      {!rows.length && rows.length === 0 && (
        <EmptyState title="No Topic history">
          {!topicOverviewIsRefetching && <>This Topic contains no history.</>}
          {topicOverviewIsRefetching && (
            <div style={{ width: "650px", paddingLeft: "200px" }}>
              <Skeleton />
            </div>
          )}
        </EmptyState>
      )}
      {topicOverviewIsRefetching && rows.length > 0 && (
        <LoadingTable rowLength={rows.length} columns={loadingColumns} />
      )}

      {!topicOverviewIsRefetching && rows.length > 0 && (
        <DataTable
          ariaLabel={"Topic history"}
          columns={columns}
          rows={rows}
          noWrap={false}
        />
      )}
    </>
  );
}

export { TopicHistory };
