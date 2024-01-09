import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { ActivityLog } from "src/domain/requests";

type ActivityLogsTableRow = {
  id: ActivityLog["req_no"];
  activityName: ActivityLog["activityName"];
  activityType: ActivityLog["activityType"];
  environmentName: ActivityLog["envName"];
  user: ActivityLog["user"];
  team: ActivityLog["team"];
  details: ActivityLog["details"];
  activityTimeString: ActivityLog["activityTimeString"];
};

type Props = {
  ariaLabel: string;
  activityLogs: ActivityLog[];
};

function ActivityLogTable({ ariaLabel, activityLogs }: Props) {
  const columns: Array<DataTableColumn<ActivityLogsTableRow>> = [
    { type: "text", field: "activityName", headerName: "Activity" },
    {
      type: "status",
      headerName: "Type",
      status: ({ activityType }) => ({
        status: "neutral",
        text: activityType,
      }),
    },
    {
      type: "status",
      headerName: "Environment",
      status: ({ environmentName }) => ({
        status: "neutral",
        text: environmentName,
      }),
    },
    { type: "text", field: "user", headerName: "User" },
    { type: "text", field: "team", headerName: "Team" },
    { type: "text", field: "details", headerName: "Details" },
    {
      type: "text",
      field: "activityTimeString",
      headerName: "Date",
      formatter: (value) => {
        return `${value}${"\u00A0"}UTC`;
      },
    },
  ];

  const rows: ActivityLogsTableRow[] = activityLogs.map(
    (activity: ActivityLog) => {
      return {
        id: activity.req_no,
        activityName: activity.activityName,
        activityType: activity.activityType,
        environmentName: activity.envName,
        user: activity.user,
        team: activity.team,
        details: activity.details,
        activityTimeString: activity.activityTimeString,
      };
    }
  );

  if (!rows.length) {
    return (
      <EmptyState title="No activity log">
        No activity log matched your criteria.
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

export { ActivityLogTable, type Props as ActivityLogTableProps };
